package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CreateOrderResponseDTO;
import edu.zut.bookrider.dto.OrdersResponseDTO;
import edu.zut.bookrider.exception.OrderNotFoundException;
import edu.zut.bookrider.mapper.order.OrderMapper;
import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.OrderItem;
import edu.zut.bookrider.model.ShoppingCartItem;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.OrderItemStatus;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;

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

    public OrdersResponseDTO getUserOrders(Authentication authentication) {

        User user = userService.getUser(authentication);

        List<Order> activeOrders = orderRepository.findByUserIdAndStatusIn(
                user.getId(), List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED));

        List<Order> completedOrders = orderRepository.findByUserIdAndStatusIn(
                user.getId(), List.of(OrderStatus.DECLINED, OrderStatus.DELIVERED));

        List<CreateOrderResponseDTO> activeOrderDtos = activeOrders.stream()
                .map(orderMapper::map)
                .toList();

        List<CreateOrderResponseDTO> completedOrderDtos = completedOrders.stream()
                .map(orderMapper::map)
                .toList();

        return new OrdersResponseDTO(activeOrderDtos, completedOrderDtos);
    }

    public OrdersResponseDTO getLibraryOrders(Authentication authentication) {

        User librarian = userService.getUser(authentication);

        List<Order> activeOrders = orderRepository.findByLibraryIdAndStatusIn(
                librarian.getLibrary().getId(), List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED));

        List<Order> completedOrders = orderRepository.findByLibraryIdAndStatusIn(
                librarian.getLibrary().getId(), List.of(OrderStatus.DECLINED, OrderStatus.DELIVERED));

        List<CreateOrderResponseDTO> activeOrderDtos = activeOrders.stream()
                .map(orderMapper::map)
                .toList();

        List<CreateOrderResponseDTO> completedOrderDtos = completedOrders.stream()
                .map(orderMapper::map)
                .toList();

        return new OrdersResponseDTO(activeOrderDtos, completedOrderDtos);
    }

    @Transactional
    public void acceptOrder(Integer orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.DECLINED) {
            throw new IllegalStateException("Order cannot be accepted because it is already accepted or declined");
        }

        User librarian = userService.getUser();

        order.setStatus(OrderStatus.ACCEPTED);
        order.setLibrarian(librarian);
        order.setAcceptedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public void declineOrder(Integer orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.DECLINED) {
            throw new IllegalStateException("Order cannot be accepted because it is already accepted or declined");
        }

        User librarian = userService.getUser();

        order.setStatus(OrderStatus.DECLINED);
        order.setLibrarian(librarian);
        orderRepository.save(order);
    }

    @Transactional
    public void handoverOrderToDriver(Integer orderId, String providedDriverId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order cannot be handed over unless it is in ACCEPTED state.");
        }

        if (!order.getDriver().getId().equals(providedDriverId)) {
            throw new IllegalArgumentException("Provided driver ID does not match the driver assigned to the order.");
        }

        order.setStatus(OrderStatus.IN_TRANSIT);
        order.setPickedUpAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
