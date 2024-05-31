package com.subride.transfer.persistent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transfer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transfer_date")
    private LocalDate transferDate;
}