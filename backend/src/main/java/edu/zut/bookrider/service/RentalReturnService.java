package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.exception.RentalAlreadyReturnedException;
import edu.zut.bookrider.exception.RentalReturnNotFoundException;
import edu.zut.bookrider.mapper.rental.RentalMapper;
import edu.zut.bookrider.mapper.rentalReturn.RentalReturnMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.model.enums.RentalReturnStatus;
import edu.zut.bookrider.repository.RentalReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.zut.bookrider.util.LateFeeCalculatorUtil.calculateLateFee;

@RequiredArgsConstructor
@Service
public class RentalReturnService {

    private final AddressService addressService;
    private final DeliveryCostCalculatorService deliveryCostCalculatorService;
    private final RentalReturnItemService rentalReturnItemService;
    private final RentalService rentalService;
    private final OrderService orderService;
    private final UserService userService;
    private final TransactionService transactionService;

    private final RentalReturnRepository rentalReturnRepository;
    private final RentalReturnMapper rentalReturnMapper;
    private final RentalMapper rentalMapper;

    private BigDecimal processLateFeesForRentals(List<RentalWithQuantityDTO> rentals,
                                                 List<RentalLateFeeDTO> lateFeeBreakdown) {
        BigDecimal totalLateFees = BigDecimal.ZERO;
        for (RentalWithQuantityDTO rentalDTO : rentals) {
            BigDecimal lateFee = calculateLateFee(rentalDTO.getRental());
            if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                RentalDTO rentalDetails = rentalMapper.map(rentalDTO.getRental());
                lateFeeBreakdown.add(new RentalLateFeeDTO(rentalDetails, lateFee));
                totalLateFees = totalLateFees.add(lateFee);
            }
        }
        return totalLateFees;
    }

    public RentalReturnPriceResponseDTO calculateReturnPrice(GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO) {

        Address pickupAddress = addressService.findOrCreateAddress(generalRentalReturnRequestDTO.getCreateAddressDTO());

        Map<Library, List<RentalWithQuantityDTO>> rentalsByLibrary = rentalService.groupRentalsByLibrary(generalRentalReturnRequestDTO.getRentalReturnRequests());

        BigDecimal totalDeliveryCost = BigDecimal.ZERO;
        BigDecimal totalLateFees = BigDecimal.ZERO;
        List<RentalLateFeeDTO> lateFeeBreakdown = new ArrayList<>();

        for (Map.Entry<Library, List<RentalWithQuantityDTO>> entry : rentalsByLibrary.entrySet()) {
            Library library = entry.getKey();
            List<RentalWithQuantityDTO> rentalsForLibrary = entry.getValue();

            BigDecimal returnOrderPrice = deliveryCostCalculatorService.calculateReturnDeliveryCost(
                    rentalsForLibrary,
                    pickupAddress,
                    library
            );
            totalDeliveryCost = totalDeliveryCost.add(returnOrderPrice);

            totalLateFees = totalLateFees.add(processLateFeesForRentals(rentalsForLibrary, lateFeeBreakdown));
        }

        totalDeliveryCost = totalDeliveryCost.setScale(2, RoundingMode.CEILING);
        totalLateFees = totalLateFees.setScale(2, RoundingMode.CEILING);

        BigDecimal totalPrice = totalDeliveryCost.add(totalLateFees).setScale(2, RoundingMode.CEILING);

        return new RentalReturnPriceResponseDTO(totalPrice, totalDeliveryCost, totalLateFees, lateFeeBreakdown);
    }

    public RentalReturnPriceResponseDTO calculateInPersonReturnPrice(InPersonRentalReturnRequestDTO inPersonRentalReturnRequestDTO) {

        List<RentalWithQuantityDTO> rentalWithQuantityDTOList = inPersonRentalReturnRequestDTO.getRentalReturnRequests().stream()
                .map(requestDTO -> {
                    Rental rental = rentalService.getRentalById(requestDTO.getRentalId());
                    return new RentalWithQuantityDTO(rental, requestDTO.getQuantityToReturn());
                }).toList();

        List<RentalLateFeeDTO> lateFeeBreakdown = new ArrayList<>();
        BigDecimal totalLateFees = processLateFeesForRentals(rentalWithQuantityDTOList, lateFeeBreakdown);

        BigDecimal totalPrice = totalLateFees;

        return new RentalReturnPriceResponseDTO(totalPrice, BigDecimal.ZERO, totalLateFees, lateFeeBreakdown);
    }

    public void markAsCompleted(Integer rentalReturnId) {

        RentalReturn rentalReturn = rentalReturnRepository.findById(rentalReturnId)
                .orElseThrow(() -> new RentalReturnNotFoundException("Rental return not found with ID: " + rentalReturnId));

        rentalService.updateRentalQuantities(rentalReturn.getRentalReturnItems());

        rentalReturn.setStatus(RentalReturnStatus.COMPLETED);

        Order returnOrder = rentalReturn.getReturnOrder();
        orderService.completeReturnOrder(returnOrder);
    }

    @Transactional
    public List<RentalReturnDTO> createRentalReturn(GeneralRentalReturnRequestDTO rentalReturnRequestDTO) {

        for (RentalReturnWithQuantityRequestDTO requestDTO : rentalReturnRequestDTO.getRentalReturnRequests()) {
            Rental rental = rentalService.getRentalById(requestDTO.getRentalId());

            boolean isAlreadyReturned = rentalReturnItemService.isAlreadyReturned(rental);
            if (isAlreadyReturned) {
                throw new RentalAlreadyReturnedException("Rental " + rental.getId() + " has already been returned.");
            }
        }

        User user = userService.getUser();
        Address pickupAddress = addressService.findOrCreateAddress(rentalReturnRequestDTO.getCreateAddressDTO());

        Map<Library, List<RentalWithQuantityDTO>> rentalsByLibrary =
                rentalService.groupRentalsByLibrary(rentalReturnRequestDTO.getRentalReturnRequests());

        List<RentalReturn> createdRentalReturns = new ArrayList<>();

        for (Map.Entry<Library, List<RentalWithQuantityDTO>> entry : rentalsByLibrary.entrySet()) {
            Library library = entry.getKey();
            List<RentalWithQuantityDTO> rentalsForLibrary = entry.getValue();

            Order returnOrder = orderService.createReturnOrder(rentalsForLibrary, pickupAddress, library);

            RentalReturn rentalReturn = createRentalReturnForOrder(returnOrder, rentalsForLibrary);

            BigDecimal totalLateFees = BigDecimal.ZERO;
            for (RentalWithQuantityDTO rentalWithQuantityDTO : rentalsForLibrary) {
                BigDecimal lateFee = calculateLateFee(rentalWithQuantityDTO.getRental());
                if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                    totalLateFees = totalLateFees.add(lateFee);
                }
            }

            if (totalLateFees.compareTo(BigDecimal.ZERO) > 0) {
                transactionService.createUserLateFeeTransaction(user, totalLateFees, rentalReturn);
            }

            transactionService.createUserPaymentTransaction(user, returnOrder);
            orderService.updateOrderPaymentStatus(returnOrder, PaymentStatus.COMPLETED);

            createdRentalReturns.add(rentalReturn);
        }

        return createdRentalReturns.stream()
                .map(rentalReturnMapper::map)
                .toList();
    }

    @Transactional
    public RentalReturn createRentalReturnForOrder(Order returnOrder, List<RentalWithQuantityDTO> rentals) {

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturn savedRentalReturn = rentalReturnRepository.save(rentalReturn);

        List<RentalReturnItem> rentalReturnItems = rentalReturnItemService.createRentalReturnItems(savedRentalReturn, rentals);
        savedRentalReturn.setRentalReturnItems(rentalReturnItems);

        return rentalReturnRepository.save(rentalReturn);
    }

    public RentalReturnDTO getLatestReturnByDriver(String driverId) {

        Order order = orderService.getSingleInRealizationOrder(driverId);

        RentalReturn rentalReturn = rentalReturnRepository.findFirstByReturnOrderOrderByReturnedAtDesc(order)
                .orElseThrow(() -> new RentalReturnNotFoundException("No rental return found for this driver."));

        return rentalReturnMapper.map(rentalReturn);
    }

    @Transactional
    public List<RentalReturnDTO> createInPersonRentalReturn(InPersonRentalReturnRequestDTO inPersonRentalReturnRequest) {

        Map<Library, List<RentalWithQuantityDTO>> rentalsByLibrary =
                rentalService.groupRentalsByLibrary(inPersonRentalReturnRequest.getRentalReturnRequests());

        List<RentalReturn> createdRentalReturns = new ArrayList<>();

        for (Map.Entry<Library, List<RentalWithQuantityDTO>> entry : rentalsByLibrary.entrySet()) {
            List<RentalWithQuantityDTO> rentalsForLibrary = entry.getValue();

            RentalReturn rentalReturn = new RentalReturn();
            rentalReturn.setStatus(RentalReturnStatus.IN_PERSON);
            RentalReturn savedRentalReturn = rentalReturnRepository.save(rentalReturn);

            List<RentalReturnItem> rentalReturnItems = rentalReturnItemService.createRentalReturnItems(savedRentalReturn, rentalsForLibrary);
            savedRentalReturn.setRentalReturnItems(rentalReturnItems);

            BigDecimal totalLateFees = BigDecimal.ZERO;
            for (RentalWithQuantityDTO rentalWithQuantityDTO : rentalsForLibrary) {
                BigDecimal lateFee = calculateLateFee(rentalWithQuantityDTO.getRental());
                if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                    totalLateFees = totalLateFees.add(lateFee);
                }
            }

            if (totalLateFees.compareTo(BigDecimal.ZERO) > 0) {
                transactionService.createUserLateFeeTransaction(rentalsForLibrary.get(0).getRental().getUser(), totalLateFees, savedRentalReturn);
            }

            createdRentalReturns.add(savedRentalReturn);
        }

        return createdRentalReturns.stream()
                .map(rentalReturnMapper::map)
                .toList();
    }

    public PageResponseDTO<RentalReturnDTO> getRentalReturns(Pageable pageable) {

        User user = userService.getUser();
        Page<RentalReturn> rentalReturnsPage = rentalReturnRepository.findRentalReturnsByUser(user, pageable);

        List<RentalReturnDTO> rentalReturnDTOs = rentalReturnsPage.getContent()
                .stream()
                .map(rentalReturnMapper::map)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                rentalReturnDTOs,
                rentalReturnsPage.getNumber(),
                rentalReturnsPage.getSize(),
                rentalReturnsPage.getTotalElements(),
                rentalReturnsPage.getTotalPages()
        );
    }

    @Transactional
    public void confirmHandover(Integer rentalReturnId, String driverId) {

        RentalReturn rentalReturn = rentalReturnRepository.findById(rentalReturnId)
                .orElseThrow(() -> new RentalReturnNotFoundException("Rental return not found"));

        if (rentalReturn.getReturnOrder().getStatus() != OrderStatus.DRIVER_ACCEPTED) {
            throw new IllegalStateException("Order cannot be handed over unless it is in DRIVER_ACCEPTED state.");
        }

        if (!rentalReturn.getReturnOrder().getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("This driver is not assigned to the rental return.");
        }

        Order returnOrder = rentalReturn.getReturnOrder();
        orderService.updateOrderStatus(returnOrder, OrderStatus.IN_TRANSIT);
    }
}
