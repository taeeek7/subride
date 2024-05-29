package com.subride.mygrp.infra.out.entity;

import com.subride.mygrp.biz.domain.MyGroup;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "my_group")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myGroupId;
    private String myGroupName;
    private Long subId;
    private String leaderId;

    @ElementCollection
    @CollectionTable(name = "my_group_member", joinColumns = @JoinColumn(name = "my_group_id"))
    @Column(name = "member_id")
    private Set<String> memberIds;

    private String bankName;
    private String bankAccount;
    private int paymentDay;
    private String inviteCode;

    public MyGroup toDomain() {
        MyGroup myGroup = new MyGroup();
        myGroup.setMyGroupId(myGroupId);
        myGroup.setMyGroupName(myGroupName);
        myGroup.setSubId(subId);
        myGroup.setLeaderId(leaderId);
        myGroup.setMemberIds(memberIds);
        myGroup.setBankName(bankName);
        myGroup.setBankAccount(bankAccount);
        myGroup.setPaymentDay(paymentDay);
        myGroup.setInviteCode(inviteCode);
        return myGroup;
    }

    public static MyGroupEntity fromDomain(MyGroup myGroup) {
        return MyGroupEntity.builder()
                .myGroupId(myGroup.getMyGroupId())
                .myGroupName(myGroup.getMyGroupName())
                .subId(myGroup.getSubId())
                .leaderId(myGroup.getLeaderId())
                .memberIds(myGroup.getMemberIds())
                .bankName(myGroup.getBankName())
                .bankAccount(myGroup.getBankAccount())
                .paymentDay(myGroup.getPaymentDay())
                .inviteCode(myGroup.getInviteCode())
                .build();
    }
}
