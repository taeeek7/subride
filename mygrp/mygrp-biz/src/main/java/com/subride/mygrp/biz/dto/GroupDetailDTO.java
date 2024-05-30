package com.subride.mygrp.biz.dto;

import com.subride.common.dto.MemberInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class GroupDetailDTO {
    private Long groupId;
    private String groupName;
    private Long subId;
    private String leaderId;
    private Set<String> memberIds;
    private String bankName;
    private String bankAccount;
    private int paymentDay;
    private String inviteCode;

    private String subName;
    private String logo;
    private Long fee;
    private int maxShareNum;

    private List<MemberInfoDTO> members;
}