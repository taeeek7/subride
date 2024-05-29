package com.subride.mygrp.biz.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class MyGroup {
    private Long myGroupId;
    private String myGroupName;
    private Long subId;
    private String leaderId;
    private String bankName;
    private String bankAccount;
    private String inviteCode;
    private int paymentDay;
    private int maxMemberCount;
    private Set<String> memberIds;
}