package com.subride.member.biz.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
    private String userId;
    private String userName;
    private String bankName;
    private String bankAccount;
    private int characterId;

    public boolean canbeAccessed() {
        return !userId.equalsIgnoreCase("user99");
    }
}
