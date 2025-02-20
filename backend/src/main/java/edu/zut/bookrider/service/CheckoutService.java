package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.OrderResponseDTO;
import edu.zut.bookrider.exception.EmptyCartException;
import edu.zut.bookrider.mapper.order.OrderMapper;
import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.ShoppingCart;
import edu.zut.bookrider.model.ShoppingCartItem;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CheckoutService {

    private final UserService userService;
    private final OrderService orderService;
    private final TransactionService transactionService;
    private final OrderMapper orderMapper;
    private final ShoppingCartService shoppingCartService;

    @Transactional
    public List<OrderResponseDTO> checkout(Authentication authentication) {

        User user = userService.getUser(authentication);
        ShoppingCart cart = user.getShoppingCart();

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("The shopping cart is empty. Cannot proceed with checkout.");
        }

        userService.validateSufficientBalance(user, cart.getTotalCartDeliveryCost());

        List<ShoppingCartItem> shoppingCartItems = new ArrayList<>(cart.getItems());
        List<Order> createdOrders = new ArrayList<>();

        for (ShoppingCartItem cartItem : shoppingCartItems) {
            Order order = orderService.createOrderFromCartItem(cartItem);
            transactionService.createUserPaymentTransaction(user, order);
            order = orderService.updateOrderPaymentStatus(order, PaymentStatus.COMPLETED);

            createdOrders.add(order);
        }

        shoppingCartService.removeAllItemsFromCart(authentication);

        return createdOrders.stream()
                .map(orderMapper::map)
                .toList();
    }
}
