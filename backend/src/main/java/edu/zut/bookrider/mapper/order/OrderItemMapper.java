package edu.zut.bookrider.mapper.order;

import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.dto.OrderItemResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderItemMapper implements Mapper<OrderItem, OrderItemResponseDTO> {

    private final BookReadMapper bookReadMapper;

    @Override
    public OrderItemResponseDTO map(OrderItem orderItem) {

        BookResponseDto bookResponseDto = bookReadMapper.map(orderItem.getBook());

        return new OrderItemResponseDTO(
                bookResponseDto,
                orderItem.getQuantity()
        );
    }
}
