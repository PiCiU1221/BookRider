package edu.zut.bookrider.mapper.rental;

import edu.zut.bookrider.dto.RentalDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.Rental;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalMapper implements Mapper<Rental, RentalDTO> {

    private final BookReadMapper bookReadMapper;

    @Override
    public RentalDTO map(Rental rental) {

        return new RentalDTO(
                rental.getId(),
                bookReadMapper.map(rental.getBook()),
                rental.getLibrary().getName(),
                rental.getLibrary().getAddress().getStreet(),
                rental.getOrder().getId(),
                rental.getQuantity(),
                rental.getRentedAt(),
                rental.getReturnDeadline(),
                rental.getStatus().toString()
        );
    }
}
