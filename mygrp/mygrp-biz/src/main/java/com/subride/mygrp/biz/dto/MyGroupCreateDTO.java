package com.subride.mygrp.biz.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyGroupCreateDTO {
    private String myGroupName;
    private Long subId;
    private String leaderId;
    private String bankName;
    private String bankAccount;
    private int paymentDay;
}