package com.subride.transfer.common.dto;


import com.subride.transfer.common.enums.Period;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {
    private Long groupId;
    private Period period;
}