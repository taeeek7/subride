package com.subride.mygrp.biz.dto;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.SubInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MyGroupDetailDTO {
    private Long myGroupId;
    private String myGroupName;
    private String inviteCode;
    private int paymentDay;
    private int memberCount;
    private int maxMemberCount;
    private String bankName;
    private String bankAccount;
    private SubInfoDTO subInfo;
    private MemberInfoDTO leaderInfo;
    private List<MemberInfoDTO> members;
}