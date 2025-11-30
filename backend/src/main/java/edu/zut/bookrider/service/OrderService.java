package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.exception.OrderNotFoundException;
import edu.zut.bookrider.mapper.coordinate.CoordinateMapper;
import edu.zut.bookrider.mapper.order.OrderMapper;
import edu.zut.bookrider.mapper.order.OrderWithPhotoMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.model.enums.TransactionType;
import edu.zut.bookrider.repository.OrderRepository;
import edu.zut.bookrider.security.websocket.WebSocketHandler;
import edu.zut.bookrider.util.BASE64DecodedMultipartFile;
import edu.zut.bookrider.util.LocationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static edu.zut.bookrider.config.SystemConstants.SERVICE_FEE_PERCENTAGE;
import static edu.zut.bookrider.config.SystemConstants.URGENT_ORDER_THRESHOLD_MINUTES;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final UserService userService;
    private final OrderMapper orderMapper;
    private final ImageUploadService imageUploadService;
    private final CoordinateMapper coordinateMapper;
    private final TransactionService transactionService;
    private final LibraryCardService libraryCardService;
    private final NavigationService navigationService;
    private final DeliveryCostCalculatorService deliveryCostCalculatorService;
    private final RentalService rentalService;

    private final OrderRepository orderRepository;
    private final OrderWithPhotoMapper orderWithPhotoMapper;

    private final WebSocketHandler webSocketHandler;

    @Transactional
    public Order createOrderFromCartItem(ShoppingCartItem item) {

        User user = item.getShoppingCart().getUser();
        libraryCardService.validateLibraryCard(user.getId());

        Library library = item.getLibrary();

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(order.getLibrary().getAddress());
        order.setDestinationAddress(item.getShoppingCart().getDeliveryAddress());
        order.setIsReturn(false);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(item.getTotalItemDeliveryCost());
        order.setPaymentStatus(PaymentStatus.PENDING);

        List<OrderItem> orderItems = item.getBooks().stream()
                .map(shoppingCartSubItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setBook(shoppingCartSubItem.getBook());
                    orderItem.setQuantity(shoppingCartSubItem.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        Integer libraryId = library.getId();
        List<User> librarians = userService.getAllLibrarians(libraryId);

        for (User librarian : librarians) {
            String username = librarian.getUsername() + ":" + libraryId;
            webSocketHandler.sendRefreshSignal(username, "librarian/orders/pending");
        }

        return orderRepository.save(order);
    }

    public Order updateOrderPaymentStatus(Order order, PaymentStatus paymentStatus) {

        order.setPaymentStatus(paymentStatus);

        return orderRepository.save(order);
    }

    private PageResponseDTO<UserOrderResponseDTO> mapToUserOrderResponseDTO(PageResponseDTO<OrderResponseDTO> orderPage) {
        List<UserOrderResponseDTO> content = orderPage.getContent().stream()
                .map(order -> {
                    Integer orderId = order.getOrderId();
                    BigDecimal lateFee = transactionService.getLateFeeTransactionAmountByOrderId(orderId);
                    BigDecimal bookPayment = transactionService.getTransactionAmountByOrderIdAndType(orderId, TransactionType.BOOK_ORDER_PAYMENT);
                    BigDecimal total = lateFee.add(bookPayment);

                    return new UserOrderResponseDTO(total, order);
                })
                .toList();

        return new PageResponseDTO<>(
                content,
                orderPage.getCurrentPage(),
                orderPage.getPageSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    public PageResponseDTO<UserOrderResponseDTO> getUserPendingOrders(int page, int size) {
        return mapToUserOrderResponseDTO(
                getUserOrdersByStatus(List.of(OrderStatus.PENDING), page, size)
        );
    }

    public PageResponseDTO<UserOrderResponseDTO> getUserInRealizationOrders(int page, int size) {
        return mapToUserOrderResponseDTO(
                getUserOrdersByStatus(List.of(OrderStatus.ACCEPTED, OrderStatus.DRIVER_ACCEPTED, OrderStatus.IN_TRANSIT), page, size)
        );
    }

    public PageResponseDTO<UserOrderResponseDTO> getUserCompletedOrders(int page, int size) {
        return mapToUserOrderResponseDTO(
                getUserOrdersByStatus(List.of(OrderStatus.DELIVERED, OrderStatus.DECLINED), page, size)
        );
    }

    private PageResponseDTO<OrderResponseDTO> getUserOrdersByStatus(
            List<OrderStatus> statuses, int page, int size) {

        User user = userService.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders = orderRepository.findByUserIdAndStatusIn(user.getId(), statuses, pageable);

        List<OrderResponseDTO> orderDtos = orders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                orderDtos,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages()
        );
    }

    public PageResponseDTO<OrderResponseDTO> getLibraryPendingOrders(int page, int size) {
        return getLibraryOrdersByStatus(List.of(OrderStatus.PENDING), page, size);
    }

    public PageResponseDTO<OrderResponseDTO> getLibraryInRealizationOrders(int page, int size) {
        return getLibraryOrdersByStatus(List.of(OrderStatus.ACCEPTED, OrderStatus.DRIVER_ACCEPTED), page, size);
    }

    public PageResponseDTO<OrderResponseDTO> getLibraryCompletedOrders(int page, int size) {
        return getLibraryOrdersByStatus(
                List.of(OrderStatus.IN_TRANSIT, OrderStatus.DELIVERED, OrderStatus.DECLINED),
                page, size);
    }

    private PageResponseDTO<OrderResponseDTO> getLibraryOrdersByStatus(
            List<OrderStatus> statuses, int page, int size) {

        User librarian = userService.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders = orderRepository.findByLibraryIdAndStatusIn(
                librarian.getLibrary().getId(), statuses, pageable);

        List<OrderResponseDTO> orderDtos = orders.getContent().stream()
                .map(orderMapper::map)
                .toList();

        return new PageResponseDTO<>(
                orderDtos,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages()
        );
    }

    @Transactional
    public void acceptOrder(Integer orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!((order.getStatus() == OrderStatus.DRIVER_ACCEPTED && order.getIsReturn()) ||
                (order.getStatus() == OrderStatus.PENDING && !order.getIsReturn()))) {
            throw new IllegalStateException("Order cannot be accepted because it is not in the correct status for handover.");
        }

        User librarian = userService.getUser();

        order.setStatus(OrderStatus.ACCEPTED);
        order.setLibrarian(librarian);
        order.setAcceptedAt(LocalDateTime.now());
        orderRepository.save(order);

        User user = order.getUser();
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/in-realization");
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/pending");
    }

    @Transactional
    public void declineOrder(Integer orderId, @Valid DeclineOrderRequestDTO declineOrderRequestDTO) {

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

        User user = order.getUser();
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/delivered");
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/pending");
    }

    @Transactional
    public void handoverOrderToDriver(Integer orderId, String providedDriverId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DRIVER_ACCEPTED) {
            throw new IllegalStateException("Order cannot be handed over unless it is in DRIVER_ACCEPTED state.");
        }

        if (order.getIsReturn()) {
            throw new IllegalStateException("Order cannot be handed over as a librarian for a return order");
        }

        if (!order.getDriver().getId().equals(providedDriverId)) {
            throw new IllegalArgumentException("Provided driver ID does not match the driver assigned to the order.");
        }

        order.setStatus(OrderStatus.IN_TRANSIT);
        order.setPickedUpAt(LocalDateTime.now());
        orderRepository.save(order);

        User user = order.getUser();
        User driver = order.getDriver();
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/in-realization");
        webSocketHandler.sendRefreshSignal(driver.getEmail(), "driver/delivery/in-realization");
    }

    public PageResponseDTO<OrderResponseDTO> getDriverPendingOrdersWithDistance(
            @Valid CoordinateDTO location, double maxDistanceInMeters, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        LocalDateTime olderThanDate = LocalDateTime.now().minusMinutes(URGENT_ORDER_THRESHOLD_MINUTES);
        Page<Order> pendingOrders = orderRepository.findOrdersForDriverWithDistance(
                BigDecimal.valueOf(location.getLatitude()),
                BigDecimal.valueOf(location.getLongitude()),
                maxDistanceInMeters,
                olderThanDate,
                pageable
        );

        List<OrderResponseDTO> pendingOrderDtos = pendingOrders.getContent().stream().map(order -> {
            OrderResponseDTO dto = orderMapper.map(order);
            BigDecimal userPayment = transactionService.getTransactionAmountByOrderIdAndType(order.getId(), TransactionType.BOOK_ORDER_PAYMENT);
            BigDecimal netAmount = userPayment.subtract(userPayment.multiply(SERVICE_FEE_PERCENTAGE));
            dto.setAmount(netAmount);

            return dto;
        }).toList();

        return new PageResponseDTO<>(
                pendingOrderDtos,
                pendingOrders.getNumber(),
                pendingOrders.getSize(),
                pendingOrders.getTotalElements(),
                pendingOrders.getTotalPages()
        );
    }

    public PageResponseDTO<OrderResponseDTO> getDriverInRealizationOrders(int page, int size) {
        User driver = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> inRealizationOrders = orderRepository.findByDriverIdAndStatusIn(
                driver.getId(), List.of(OrderStatus.DRIVER_ACCEPTED, OrderStatus.IN_TRANSIT, OrderStatus.AWAITING_LIBRARY_CONFIRMATION), pageable);

        List<OrderResponseDTO> inRealizationOrderDtos = inRealizationOrders.getContent().stream().map(order -> {
            OrderResponseDTO dto = orderMapper.map(order);
            BigDecimal userPayment = transactionService.getTransactionAmountByOrderIdAndType(order.getId(), TransactionType.BOOK_ORDER_PAYMENT);
            BigDecimal netAmount = userPayment.subtract(userPayment.multiply(SERVICE_FEE_PERCENTAGE));
            dto.setAmount(netAmount);

            return dto;
        }).toList();


        return new PageResponseDTO<>(
                inRealizationOrderDtos,
                inRealizationOrders.getNumber(),
                inRealizationOrders.getSize(),
                inRealizationOrders.getTotalElements(),
                inRealizationOrders.getTotalPages()
        );
    }

    public PageResponseDTO<OrderWithPhotoResponseDTO> getDriverCompletedOrders(int page, int size) {
        User driver = userService.getUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("deliveredAt").descending());
        Page<Order> completedOrders = orderRepository.findByDriverIdAndStatusIn(
                driver.getId(), List.of(OrderStatus.DELIVERED), pageable);

        List<OrderWithPhotoResponseDTO> completedOrderDtos = completedOrders.getContent().stream().map(order -> {
            OrderWithPhotoResponseDTO dto = orderWithPhotoMapper.map(order);
            BigDecimal driverPayout = transactionService.getTransactionAmountByOrderIdAndType(order.getId(), TransactionType.DRIVER_EARNINGS);
            dto.setAmount(driverPayout);

            return dto;
        }).toList();

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

        if (!((order.getStatus() == OrderStatus.ACCEPTED && !order.getIsReturn()) ||
                (order.getStatus() == OrderStatus.PENDING && order.getIsReturn()))) {
            throw new IllegalStateException("Order cannot be picked unless it is in ACCEPTED state (for non-return) or PENDING state (for return).");
        }

        boolean hasOngoingOrder = orderRepository.existsByDriverAndStatusNot(driver, OrderStatus.DELIVERED);
        if (hasOngoingOrder) {
            throw new IllegalArgumentException("Driver can only have one order in realization at the time.");
        }

        if (!isNull(order.getDriver())) {
            throw new IllegalArgumentException("Some other driver already picked this order.");
        }

        order.setDriver(driver);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setDriverAssignedAt(LocalDateTime.now());
        orderRepository.save(order);

        User user = order.getUser();
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/in-realization");
    }

    @Transactional
    public DeliverOrderResponseDTO deliverOrder(Integer orderId, @Valid DeliverOrderRequestDTO requestDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Order is not in the correct state for delivery.");
        }

        User driver = userService.getUser();
        if (!order.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("You are not assigned to this order.");
        }

        CoordinateDTO targetCoordinates = coordinateMapper.map(order.getDestinationAddress());

        double distanceToDeliveryAddress = LocationUtils.calculateDistance(
                requestDTO.getLocation(),
                targetCoordinates
        );

        if (distanceToDeliveryAddress > 200) {
            throw new IllegalArgumentException("Driver is too far from the delivery location.");
        }

        byte[] imageBase64 = Base64.getDecoder().decode(requestDTO.getPhotoBase64());
        MultipartFile multipartFile = new BASE64DecodedMultipartFile(imageBase64);
        String deliveryPhotoUrl = imageUploadService.uploadImage(multipartFile);

        order.setDeliveryPhotoUrl(deliveryPhotoUrl);
        order.setDeliveredAt(LocalDateTime.now());

        if (order.getIsReturn()) {
            order.setStatus(OrderStatus.AWAITING_LIBRARY_CONFIRMATION);
            orderRepository.save(order);

            webSocketHandler.sendRefreshSignal(driver.getEmail(), "driver/in-realization");

            return new DeliverOrderResponseDTO(false);
        } else {
            order.setStatus(OrderStatus.DELIVERED);
            for (OrderItem item : order.getOrderItems()) {
                rentalService.createRental(item);
            }
            orderRepository.save(order);

            transactionService.createDriverPayoutTransaction(driver, order);

            User user = order.getUser();
            webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/delivered");
            webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/pending");
            webSocketHandler.sendRefreshSignal(user.getEmail(), "driver/order-history");

            return new DeliverOrderResponseDTO(true);
        }
    }

    public void completeReturnOrder(Order order) {

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);

        User driver = order.getDriver();
        transactionService.createDriverPayoutTransaction(driver, order);

        webSocketHandler.sendRefreshSignal(driver.getEmail(), "driver/in-realization");
        webSocketHandler.sendRefreshSignal(driver.getEmail(), "driver/order-history");

        User user = order.getUser();
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/delivered");
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/orders/in-realization");
        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/rental-returns");
    }

    public NavigationResponseDTO getNavigation(
            Integer orderId,
            DeliveryNavigationRequestDTO navigationRequestDTO,
            boolean isPickupNavigation) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        User driver = userService.getUser();
        if (!order.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("You are not assigned to this order.");
        }

        return navigationService.getNavigationForOrder(order, navigationRequestDTO, isPickupNavigation);
    }

    @Transactional
    public Order createReturnOrder(List<RentalWithQuantityDTO> rentals, Address pickupAddress, Library library) {
        User user = userService.getUser();
        BigDecimal returnOrderAmount = deliveryCostCalculatorService.calculateReturnDeliveryCost(
                rentals, pickupAddress, library
        );

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(pickupAddress);
        order.setDestinationAddress(order.getLibrary().getAddress());
        order.setIsReturn(true);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(returnOrderAmount);
        order.setPaymentStatus(PaymentStatus.PENDING);

        List<OrderItem> orderItems = rentals.stream()
                .map(rentalWithQuantityDTO -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setBook(rentalWithQuantityDTO.getRental().getBook());
                    orderItem.setQuantity(rentalWithQuantityDTO.getQuantityToReturn());
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }

    public Order getSingleInRealizationOrder(String driverId) {
        return orderRepository.findFirstByDriverIdAndStatusIn(
                driverId,
                List.of(OrderStatus.AWAITING_LIBRARY_CONFIRMATION)
        ).orElseThrow(() -> new OrderNotFoundException("No active order found for this driver."));
    }

    @Transactional
    public void confirmReturnHandover(Order order) {
        order.setStatus(OrderStatus.IN_TRANSIT);
        order.setPickedUpAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
