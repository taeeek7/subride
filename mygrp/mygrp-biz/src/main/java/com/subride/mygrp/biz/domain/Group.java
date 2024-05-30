package com.subride.mygrp.biz.domain;

import com.subride.common.dto.MemberInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Group {
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

    private Long payedFee;
    private Long discountedFee;

    private List<MemberInfoDTO> members;

    //-- 실제로 내고 있는 구독료 계산
    public void calulatePayedFee(String userId) {
        if (this.memberIds.contains(userId)) {
            this.payedFee = this.fee / this.memberIds.size();
        } else {
            this.payedFee = this.fee;
        }
    }

    //-- 절감할 수 있는 금액 계산
    public void calulateDiscountedFee(String userId) {
        if (this.memberIds.contains(userId)) {
            this.discountedFee = this.fee - this.fee / this.memberIds.size();
        } else {
            this.discountedFee = this.fee - this.fee / this.maxShareNum;
        }
    }
}