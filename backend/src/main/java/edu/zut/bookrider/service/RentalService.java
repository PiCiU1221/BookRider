package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.PageResponseDTO;
import edu.zut.bookrider.dto.RentalDTO;
import edu.zut.bookrider.dto.RentalReturnWithQuantityRequestDTO;
import edu.zut.bookrider.dto.RentalWithQuantityDTO;
import edu.zut.bookrider.exception.InvalidReturnQuantityException;
import edu.zut.bookrider.exception.RentalNotFoundException;
import edu.zut.bookrider.mapper.rental.RentalMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.model.enums.RentalStatus;
import edu.zut.bookrider.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.zut.bookrider.config.SystemConstants.RETURN_DEADLINE_DAYS;

@RequiredArgsConstructor
@Service
public class RentalService {

    private final UserService userService;

    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

    @Transactional
    public void createRental(OrderItem orderItem) {

        Order order = orderItem.getOrder();

        Rental rental = new Rental();
        rental.setUser(order.getUser());
        rental.setBook(orderItem.getBook());
        rental.setLibrary(order.getLibrary());
        rental.setOrder(order);
        rental.setQuantity(orderItem.getQuantity());
        rental.setReturnDeadline(LocalDateTime.now().plusDays(RETURN_DEADLINE_DAYS));
        rental.setStatus(RentalStatus.RENTED);

        Rental savedRental = rentalRepository.save(rental);

        rentalMapper.map(savedRental);
    }

    @Transactional
    public void updateRentalQuantities(List<RentalReturnItem> rentalReturnItems) {
        for (RentalReturnItem rentalReturnItem : rentalReturnItems) {
            Rental rental = rentalReturnItem.getRental();

            if (rental.getQuantity() - rentalReturnItem.getReturnedQuantity() == 0) {
                rental.setStatus(RentalStatus.RETURNED);
            } else {
                rental.setStatus(RentalStatus.PARTIALLY_RETURNED);
            }

            rentalRepository.save(rental);
        }
    }

    @Transactional
    public Map<Library, List<RentalWithQuantityDTO>> groupRentalsByLibrary(List<RentalReturnWithQuantityRequestDTO> rentalReturnRequests) {

        Map<Library, List<RentalWithQuantityDTO>> rentalsByLibrary = new HashMap<>();

        for (RentalReturnWithQuantityRequestDTO rentalRequestDTO : rentalReturnRequests) {
            Rental rental = rentalRepository.findById(rentalRequestDTO.getRentalId())
                    .orElseThrow(() -> new RentalNotFoundException("Rental not found for ID: " + rentalRequestDTO.getRentalId()));

            if (rental.getQuantity() < rentalRequestDTO.getQuantityToReturn()) {
                throw new InvalidReturnQuantityException("Amount to return exceeds rental quantity.");
            }

            Library library = rental.getLibrary();
            RentalWithQuantityDTO rentalWithQuantity = new RentalWithQuantityDTO(rental, rentalRequestDTO.getQuantityToReturn());

            rentalsByLibrary.computeIfAbsent(library, k -> new ArrayList<>()).add(rentalWithQuantity);
        }

        return rentalsByLibrary;
    }

    public Rental getRentalById(Integer rentalId) {

        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException("Rental with ID: " + rentalId + " not found."));
    }

    public PageResponseDTO<RentalDTO> getUserRentals(Pageable pageable) {

        User user = userService.getUser();
        Page<Rental> rentals = rentalRepository.findAllByUser(user, pageable);

        List<RentalDTO> rentalDTOs = rentals.getContent()
                .stream()
                .map(rentalMapper::map)
                .toList();

        return new PageResponseDTO<>(
                rentalDTOs,
                rentals.getNumber(),
                rentals.getSize(),
                rentals.getTotalElements(),
                rentals.getTotalPages()
        );
    }

    public Rental updateRentalStatus(Integer rentalId, RentalStatus status) {
        Rental rental =  getRentalById(rentalId);
        rental.setStatus(status);

        return rentalRepository.save(rental);
    }
}
