package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersResponseDTO {
    private List<CreateOrderResponseDTO> pendingOrders;
    private List<CreateOrderResponseDTO> inRealizationOrders;
    private List<CreateOrderResponseDTO> completedOrders;
}
