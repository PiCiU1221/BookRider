package edu.zut.bookrider.service;

import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.OrderItem;
import edu.zut.bookrider.model.ShoppingCartItem;
import edu.zut.bookrider.model.enums.OrderItemStatus;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrderFromCartItem(ShoppingCartItem item) {

        Order order = new Order();
        order.setUser(item.getShoppingCart().getUser());
        order.setLibrary(item.getLibrary());
        order.setTargetAddress(item.getShoppingCart().getDeliveryAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(item.getTotalItemDeliveryCost());
        order.setPaymentStatus(PaymentStatus.PENDING);

        List<OrderItem> orderItems = item.getBooks().stream()
                .map(shoppingCartSubItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setBook(shoppingCartSubItem.getBook());
                    orderItem.setQuantity(shoppingCartSubItem.getQuantity());
                    orderItem.setStatus(OrderItemStatus.BORROWED);
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        return orderRepository.save(order);
    }

    public Order updateOrderPaymentStatus(Order order, PaymentStatus paymentStatus) {

        order.setPaymentStatus(paymentStatus);

        return orderRepository.save(order);
    }
}
