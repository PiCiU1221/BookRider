package edu.zut.bookrider.mapper.rentalReturn;

import edu.zut.bookrider.dto.RentalReturnDTO;
import edu.zut.bookrider.dto.RentalReturnItemDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.RentalReturn;
import edu.zut.bookrider.model.RentalReturnItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class RentalReturnMapper implements Mapper<RentalReturn, RentalReturnDTO> {

    private final BookReadMapper bookReadMapper;

    @Override
    public RentalReturnDTO map(RentalReturn rentalReturn) {
        return new RentalReturnDTO(
                rentalReturn.getId(),
                rentalReturn.getRentalReturnItems().get(0).getRental().getLibrary().getName(),
                rentalReturn.getReturnOrder() != null ? rentalReturn.getReturnOrder().getId() : null,
                rentalReturn.getReturnedAt(),
                rentalReturn.getCreatedAt(),
                rentalReturn.getStatus(),
                rentalReturn.getRentalReturnItems().stream()
                        .map(this::mapItem)
                        .collect(Collectors.toList())
        );
    }

    private RentalReturnItemDTO mapItem(RentalReturnItem item) {
        return new RentalReturnItemDTO(
                item.getId(),
                item.getRental().getId(),
                item.getBook() != null ? bookReadMapper.map(item.getBook()) : null,
                item.getReturnedQuantity()
        );
    }
}
