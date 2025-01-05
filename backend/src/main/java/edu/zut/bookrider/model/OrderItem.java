package edu.zut.bookrider.model;

import edu.zut.bookrider.model.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity<Integer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "return_deadline")
    private LocalDateTime returnDeadline;

    @Column(name = "returned_quantity")
    private int returnedQuantity = 0;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;
}
