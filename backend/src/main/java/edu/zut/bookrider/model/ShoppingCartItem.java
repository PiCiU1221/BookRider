package edu.zut.bookrider.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@Table(name = "shopping_cart_items")
public class ShoppingCartItem extends BaseEntity<Integer> {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_cart_id", nullable = false)
    private ShoppingCart shoppingCart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Builder.Default
    @Column(name = "delivery_cost", nullable = false)
    private BigDecimal totalItemDeliveryCost = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "shoppingCartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingCartSubItem> books = new ArrayList<>();
}
