package edu.zut.bookrider.mapper.order;

import edu.zut.bookrider.dto.CreateOrderItemResponseDTO;
import edu.zut.bookrider.dto.CreateOrderResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderMapper implements Mapper<Order, CreateOrderResponseDTO> {

    private final OrderItemMapper orderItemMapper;

    @Override
    public CreateOrderResponseDTO map(Order order) {

        List<CreateOrderItemResponseDTO> orderItemDtos = order.getOrderItems().stream()
                .map(orderItemMapper::map)
                .toList();

        return new CreateOrderResponseDTO(
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
                orderItemDtos
        );
    }
}
