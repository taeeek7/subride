package com.subride.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class GroupMemberDTO {
    private Long groupId;
    private Set<String> memberIds;
    private int paymentDay;
}