package com.subride.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberInfoDTO {
    private String userId;
    private String userName;
    private String bankName;
    private String bankAccount;
    private int characterId;
}