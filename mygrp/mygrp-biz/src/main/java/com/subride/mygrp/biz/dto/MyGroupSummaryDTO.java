package com.subride.mygrp.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyGroupSummaryDTO {
    private Long myGroupId;
    private String myGroupName;
    private String subName;
    private String logo;
    private int paymentDay;
    private Long fee;
    private Long discountedFee;
}