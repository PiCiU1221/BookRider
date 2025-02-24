package edu.zut.bookrider.mapper.shoppingCart;

import edu.zut.bookrider.dto.ShoppingCartSubItemResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.ShoppingCartSubItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShoppingCartSubItemMapper implements Mapper<ShoppingCartSubItem, ShoppingCartSubItemResponseDTO> {

    private final BookReadMapper bookReadMapper;

    @Override
    public ShoppingCartSubItemResponseDTO map(ShoppingCartSubItem shoppingCartSubItem) {
        return new ShoppingCartSubItemResponseDTO(
                shoppingCartSubItem.getId(),
                bookReadMapper.map(shoppingCartSubItem.getBook()),
                shoppingCartSubItem.getQuantity()
        );
    }
}
