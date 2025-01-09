package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersResponseDTO {
    private List<CreateOrderResponseDTO> activeOrders;
    private List<CreateOrderResponseDTO> completedOrders;
}
