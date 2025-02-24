package edu.zut.bookrider.mapper.shoppingCart;

import edu.zut.bookrider.dto.ShoppingCartItemResponseDTO;
import edu.zut.bookrider.dto.ShoppingCartSubItemResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.ShoppingCartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ShoppingCartItemMapper implements Mapper<ShoppingCartItem, ShoppingCartItemResponseDTO> {

    private final ShoppingCartSubItemMapper shoppingCartSubItemMapper;

    @Override
    public ShoppingCartItemResponseDTO map(ShoppingCartItem shoppingCartItem) {

        List<ShoppingCartSubItemResponseDTO> subItemResponseDTOs = shoppingCartItem.getBooks()
                .stream()
                .map(shoppingCartSubItemMapper::map)
                .toList();

        return new ShoppingCartItemResponseDTO(
                shoppingCartItem.getLibrary().getId(),
                shoppingCartItem.getTotalItemDeliveryCost(),
                subItemResponseDTOs
        );
    }
}
