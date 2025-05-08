package edu.zut.bookrider.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@Table(name = "roles")
public class Role extends BaseEntity<Integer> {

    @Column(nullable = false, unique = true)
    private String name;
}
