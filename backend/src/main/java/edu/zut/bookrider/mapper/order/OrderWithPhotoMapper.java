package edu.zut.bookrider.mapper.order;

import edu.zut.bookrider.dto.OrderItemResponseDTO;
import edu.zut.bookrider.dto.OrderWithPhotoResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderWithPhotoMapper implements Mapper<Order, OrderWithPhotoResponseDTO> {

    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderWithPhotoResponseDTO map(Order order) {

        List<OrderItemResponseDTO> orderItemDtos = order.getOrderItems().stream()
                .map(orderItemMapper::map)
                .toList();

        return new OrderWithPhotoResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getLibrary().getName(),
                order.getPickupAddress().getStreet(),
                order.getDestinationAddress().getStreet(),
                order.getIsReturn(),
                order.getStatus().toString(),
                order.getAmount(),
                order.getPaymentStatus().toString(),
                order.getNoteToDriver(),
                order.getDeliveryPhotoUrl(),
                order.getCreatedAt(),
                order.getAcceptedAt(),
                order.getDriverAssignedAt(),
                order.getPickedUpAt(),
                order.getDeliveredAt(),
                orderItemDtos
        );
    }
}
