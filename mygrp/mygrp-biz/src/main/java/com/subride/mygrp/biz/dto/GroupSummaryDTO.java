package com.subride.mygrp.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupSummaryDTO {
    private Long groupId;
    private String groupName;
    private String subName;
    private String logo;
    private int paymentDay;
    private Long fee;
    private Long payedFee;
    private Long discountedFee;
}