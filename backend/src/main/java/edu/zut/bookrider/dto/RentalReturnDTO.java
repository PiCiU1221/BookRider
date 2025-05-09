package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.RentalReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalReturnDTO {
    private Integer id;
    private String libraryName;
    private Integer orderId;
    private OrderStatus orderStatus;
    private RentalReturnStatus status;
    private LocalDateTime returnedAt;
    private LocalDateTime createdAt;
    private List<RentalReturnItemDTO> rentalReturnItems;
}
