package com.subride.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySubInfoDTO {
    private String userId;
    private Long subId;
    private String subName;
    private String categoryName;
    private String logo;
    private String description;
    private Long fee;
    private int maxShareNum;
    private boolean joinGroup;
    private Long payedFee;
    private Long discountedFee;
}