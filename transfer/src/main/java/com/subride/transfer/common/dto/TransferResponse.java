package com.subride.transfer.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TransferResponse {
    private String memberId;
    private BigDecimal amount;
    private LocalDate transferDate;
}