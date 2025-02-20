package edu.zut.bookrider.mapper.order;

import edu.zut.bookrider.dto.OrderItemResponseDTO;
import edu.zut.bookrider.dto.OrderResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderMapper implements Mapper<Order, OrderResponseDTO> {

    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderResponseDTO map(Order order) {

        List<OrderItemResponseDTO> orderItemDtos = order.getOrderItems().stream()
                .map(orderItemMapper::map)
                .toList();

        return new OrderResponseDTO(
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
                order.getCreatedAt(),
                order.getAcceptedAt(),
                order.getPickedUpAt(),
                order.getDeliveredAt(),
                orderItemDtos
        );
    }
}
