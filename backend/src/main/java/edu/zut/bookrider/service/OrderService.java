package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.exception.OrderNotFoundException;
import edu.zut.bookrider.mapper.coordinate.CoordinateMapper;
import edu.zut.bookrider.mapper.order.OrderMapper;
import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.OrderItem;
import edu.zut.bookrider.model.ShoppingCartItem;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.OrderItemStatus;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.repository.OrderRepository;
import edu.zut.bookrider.util.BASE64DecodedMultipartFile;
import edu.zut.bookrider.util.LocationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;
    private final ImageUploadService imageUploadService;
    private final CoordinateMapper coordinateMapper;
    private final TransactionService transactionService;

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

    public PageResponseDTO<CreateOrderResponseDTO> getUserPendingOrders(int page, int size) {
        User user = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> pendingOrders = orderRepository.findByUserIdAndStatusIn(
                user.getId(), List.of(OrderStatus.PENDING), pageable);

        List<CreateOrderResponseDTO> orderDtos = pendingOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                orderDtos,
                pendingOrders.getNumber(),
                pendingOrders.getSize(),
                pendingOrders.getTotalElements(),
                pendingOrders.getTotalPages()
        );
    }

    public PageResponseDTO<CreateOrderResponseDTO> getUserInRealizationOrders(int page, int size) {
        User user = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<OrderStatus> orderStatusList = List.of(
                OrderStatus.ACCEPTED, OrderStatus.DRIVER_PICKED, OrderStatus.IN_TRANSIT_TO_CUSTOMER);
        Page<Order> inRealizationOrders = orderRepository.findByUserIdAndStatusIn(
                user.getId(), orderStatusList, pageable);

        List<CreateOrderResponseDTO> orderDtos = inRealizationOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                orderDtos,
                inRealizationOrders.getNumber(),
                inRealizationOrders.getSize(),
                inRealizationOrders.getTotalElements(),
                inRealizationOrders.getTotalPages()
        );
    }

    public PageResponseDTO<CreateOrderResponseDTO> getUserCompletedOrders(int page, int size) {
        User user = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> completedOrders = orderRepository.findByUserIdAndStatusIn(
                user.getId(), List.of(OrderStatus.DELIVERED, OrderStatus.DECLINED), pageable);

        List<CreateOrderResponseDTO> orderDtos = completedOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                orderDtos,
                completedOrders.getNumber(),
                completedOrders.getSize(),
                completedOrders.getTotalElements(),
                completedOrders.getTotalPages()
        );
    }

    public PageResponseDTO<CreateOrderResponseDTO> getLibraryPendingOrders(int page, int size) {
        User librarian = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> pendingOrders = orderRepository.findByLibraryIdAndStatusIn(
                librarian.getLibrary().getId(), List.of(OrderStatus.PENDING), pageable);

        List<CreateOrderResponseDTO> pendingOrderDtos = pendingOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                pendingOrderDtos,
                pendingOrders.getNumber(),
                pendingOrders.getSize(),
                pendingOrders.getTotalElements(),
                pendingOrders.getTotalPages()
        );
    }

    public PageResponseDTO<CreateOrderResponseDTO> getLibraryInRealizationOrders(int page, int size) {
        User librarian = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> inRealizationOrders = orderRepository.findByLibraryIdAndStatusIn(
                librarian.getLibrary().getId(), List.of(OrderStatus.ACCEPTED), pageable);

        List<CreateOrderResponseDTO> inRealizationOrderDtos = inRealizationOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                inRealizationOrderDtos,
                inRealizationOrders.getNumber(),
                inRealizationOrders.getSize(),
                inRealizationOrders.getTotalElements(),
                inRealizationOrders.getTotalPages()
        );
    }

    public PageResponseDTO<CreateOrderResponseDTO> getLibraryCompletedOrders(int page, int size) {
        User librarian = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<OrderStatus> orderStatusList = List.of(
                OrderStatus.ACCEPTED,OrderStatus.DECLINED, OrderStatus.DRIVER_PICKED, OrderStatus.IN_TRANSIT_TO_CUSTOMER, OrderStatus.DELIVERED);
        Page<Order> completedOrders = orderRepository.findByLibraryIdAndStatusIn(
                librarian.getLibrary().getId(), orderStatusList, pageable);

        List<CreateOrderResponseDTO> completedOrderDtos = completedOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                completedOrderDtos,
                completedOrders.getNumber(),
                completedOrders.getSize(),
                completedOrders.getTotalElements(),
                completedOrders.getTotalPages()
        );
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
    public void declineOrder(Integer orderId, DeclineOrderRequestDTO declineOrderRequestDTO) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.DECLINED) {
            throw new IllegalStateException("Order cannot be accepted because it is already accepted or declined");
        }

        User librarian = userService.getUser();

        order.setStatus(OrderStatus.DECLINED);
        order.setLibrarian(librarian);
        order.setDeclineReason(declineOrderRequestDTO.getReason());
        orderRepository.save(order);
    }

    @Transactional
    public void handoverOrderToDriver(Integer orderId, String providedDriverId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DRIVER_PICKED) {
            throw new IllegalStateException("Order cannot be handed over unless it is in DRIVER_PICKED state.");
        }

        if (!order.getDriver().getId().equals(providedDriverId)) {
            throw new IllegalArgumentException("Provided driver ID does not match the driver assigned to the order.");
        }

        order.setStatus(OrderStatus.IN_TRANSIT_TO_CUSTOMER);
        order.setPickedUpAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public PageResponseDTO<CreateOrderResponseDTO> getDriverPendingOrdersWithDistance(
            CoordinateDTO location, double maxDistanceInMeters, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> pendingOrders = orderRepository.findAcceptedOrdersForDriverWithDistance(
                OrderStatus.ACCEPTED, BigDecimal.valueOf(location.getLatitude()),
                BigDecimal.valueOf(location.getLongitude()), maxDistanceInMeters, pageable);

        List<CreateOrderResponseDTO> pendingOrderDtos = pendingOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                pendingOrderDtos,
                pendingOrders.getNumber(),
                pendingOrders.getSize(),
                pendingOrders.getTotalElements(),
                pendingOrders.getTotalPages()
        );
    }

    public PageResponseDTO<CreateOrderResponseDTO> getDriverInRealizationOrders(int page, int size) {
        User driver = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> inRealizationOrders = orderRepository.findByDriverIdAndStatusIn(
                driver.getId(), List.of(OrderStatus.DRIVER_PICKED, OrderStatus.IN_TRANSIT_TO_CUSTOMER), pageable);

        List<CreateOrderResponseDTO> inRealizationOrderDtos = inRealizationOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                inRealizationOrderDtos,
                inRealizationOrders.getNumber(),
                inRealizationOrders.getSize(),
                inRealizationOrders.getTotalElements(),
                inRealizationOrders.getTotalPages()
        );
    }

    public PageResponseDTO<CreateOrderResponseDTO> getDriverCompletedOrders(int page, int size) {
        User driver = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> completedOrders = orderRepository.findByDriverIdAndStatusIn(
                driver.getId(), List.of(OrderStatus.DELIVERED), pageable);

        List<CreateOrderResponseDTO> completedOrderDtos = completedOrders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                completedOrderDtos,
                completedOrders.getNumber(),
                completedOrders.getSize(),
                completedOrders.getTotalElements(),
                completedOrders.getTotalPages()
        );
    }

    public void assignDriverToOrder(Integer orderId) {
        User driver = userService.getUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order cannot be picked unless it is in ACCEPTED state.");
        }

        if (!isNull(order.getDriver())) {
            throw new IllegalArgumentException("Some other driver already picked this order.");
        }

        order.setDriver(driver);
        order.setStatus(OrderStatus.DRIVER_PICKED);
        order.setDriverAssignedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public CreateTransactionResponseDTO deliverOrder(Integer orderId, DeliverOrderRequestDTO requestDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.IN_TRANSIT_TO_CUSTOMER) {
            throw new IllegalArgumentException("Order is not in the correct state for delivery.");
        }

        User driver = userService.getUser();
        if (!order.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("You are not assigned to this order.");
        }

        byte[] imageBase64 = Base64.getDecoder().decode(requestDTO.getPhotoBase64());
        MultipartFile multipartFile = new BASE64DecodedMultipartFile(imageBase64);
        String deliveryPhotoUrl;

        try {
            deliveryPhotoUrl = imageUploadService.uploadImage(multipartFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CoordinateDTO targetCoordinates = coordinateMapper.map(order.getTargetAddress());

        double distanceToDeliveryAddress = LocationUtils.calculateDistance(
                requestDTO.getLocation(),
                targetCoordinates
        );

        if (distanceToDeliveryAddress > 200) {
            throw new IllegalArgumentException("Driver is too far from the delivery location.");
        }

        order.setDeliveryPhotoUrl(deliveryPhotoUrl);
        order.setDeliveredAt(LocalDateTime.now());
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        return transactionService.createDriverPayoutTransaction(driver, order);
    }
}
