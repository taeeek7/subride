package com.subride.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupSummaryDTO {
    private Long groupId;
    private String groupName;
    private Long subId;
    private String subName;
    private String logo;
    private int paymentDay;
    private Long fee;
    private int memberCount;
}