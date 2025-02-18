package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.RentalWithQuantityDTO;
import edu.zut.bookrider.model.Rental;
import edu.zut.bookrider.model.RentalReturn;
import edu.zut.bookrider.model.RentalReturnItem;
import edu.zut.bookrider.repository.RentalReturnItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RentalReturnItemService {

    private final RentalReturnItemRepository rentalReturnItemRepository;

    public List<RentalReturnItem> createRentalReturnItems(RentalReturn rentalReturn, List<RentalWithQuantityDTO> rentals) {
        List<RentalReturnItem> returnItems = rentals.stream().map(rentalDto -> {
            RentalReturnItem returnItem = new RentalReturnItem();
            returnItem.setRentalReturn(rentalReturn);
            returnItem.setRental(rentalDto.getRental());
            returnItem.setBook(rentalDto.getRental().getBook());
            returnItem.setReturnedQuantity(rentalDto.getQuantityToReturn());
            return returnItem;
        }).collect(Collectors.toList());

        rentalReturnItemRepository.saveAll(returnItems);
        return returnItems;
    }

    public boolean isAlreadyReturned(Rental rental) {
        return rentalReturnItemRepository.existsByRental(rental);
    }
}
