package com.subride.member.infra.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class SignupRequestDTO {
    private String userId;
    private String password;
    private Set<String> roles;
    private String userName;
    private String bankName;
    private String bankAccount;

}
