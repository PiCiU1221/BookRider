package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.RentalWithQuantityDTO;
import edu.zut.bookrider.exception.RentalAlreadyReturnedException;
import edu.zut.bookrider.model.Rental;
import edu.zut.bookrider.model.RentalReturn;
import edu.zut.bookrider.model.RentalReturnItem;
import edu.zut.bookrider.repository.RentalReturnItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RentalReturnItemService {

    private final RentalReturnItemRepository rentalReturnItemRepository;

    public List<RentalReturnItem> createRentalReturnItems(RentalReturn rentalReturn, List<RentalWithQuantityDTO> rentals) {
        List<Integer> rentalIds = rentals.stream()
                .map(r -> r.getRental().getId())
                .toList();

        List<Object[]> results = rentalReturnItemRepository.sumReturnedQuantitiesByRentalIds(rentalIds);

        Map<Integer, Integer> returnedQuantities = results.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        List<RentalReturnItem> returnItems = new ArrayList<>();

        for (RentalWithQuantityDTO dto : rentals) {
            Rental rental = dto.getRental();
            int alreadyReturned = returnedQuantities.getOrDefault(rental.getId(), 0);
            int remaining = rental.getQuantity() - alreadyReturned;
            int toReturn = dto.getQuantityToReturn();

            if (toReturn < 1 || toReturn > remaining) {
                throw new RentalAlreadyReturnedException("Invalid return quantity for rental ID " + rental.getId() +
                        ". Requested: " + toReturn + ", Available to return: " + remaining);
            }

            RentalReturnItem returnItem = new RentalReturnItem();
            returnItem.setRentalReturn(rentalReturn);
            returnItem.setRental(rental);
            returnItem.setBook(rental.getBook());
            returnItem.setReturnedQuantity(toReturn);

            returnItems.add(returnItem);
        }

        rentalReturnItemRepository.saveAll(returnItems);
        return returnItems;
    }

    int sumReturnedQuantityByRentalId(Integer rentalId) {
        return rentalReturnItemRepository.sumReturnedQuantityByRentalId(rentalId);
    }
}
