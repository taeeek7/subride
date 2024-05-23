package com.subride.member.biz.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Account {
    private String userId;
    private String password;
    private Set<String> roles;
}
