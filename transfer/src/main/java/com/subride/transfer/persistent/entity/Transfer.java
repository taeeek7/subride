package com.subride.transfer.persistent.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    private Long id;
    private Long groupId;
    private String memberId;
    private BigDecimal amount;
    private LocalDate transferDate;
}
