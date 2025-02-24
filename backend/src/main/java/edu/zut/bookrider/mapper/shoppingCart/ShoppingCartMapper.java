package edu.zut.bookrider.mapper.shoppingCart;

import edu.zut.bookrider.dto.ShoppingCartItemResponseDTO;
import edu.zut.bookrider.dto.ShoppingCartResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.ShoppingCart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ShoppingCartMapper implements Mapper<ShoppingCart, ShoppingCartResponseDTO> {

    private final ShoppingCartItemMapper shoppingCartItemMapper;

    @Override
    public ShoppingCartResponseDTO map(ShoppingCart shoppingCart) {

        String addressString = shoppingCart.getDeliveryAddress() != null
                ? shoppingCart.getDeliveryAddress().getStreet() + ", " + shoppingCart.getDeliveryAddress().getCity()
                : null;

        List<ShoppingCartItemResponseDTO> itemResponseDTOs = shoppingCart.getItems()
                .stream()
                .map(shoppingCartItemMapper::map)
                .toList();

        return new ShoppingCartResponseDTO(
                shoppingCart.getTotalCartDeliveryCost(),
                addressString,
                itemResponseDTOs
        );
    }
}
