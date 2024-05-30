package com.subride.mygrp.infra.out.entity;

import com.subride.mygrp.biz.domain.Group;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "subgroup")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;
    private String groupName;
    private Long subId;
    private String leaderId;

    @ElementCollection
    @CollectionTable(name = "subgroup_member", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "member_id")
    private Set<String> memberIds;

    private String bankName;
    private String bankAccount;
    private int paymentDay;
    private String inviteCode;

    public Group toDomain() {
        Group group = new Group();
        group.setGroupId(groupId);
        group.setGroupName(groupName);
        group.setSubId(subId);
        group.setLeaderId(leaderId);
        group.setMemberIds(memberIds);
        group.setBankName(bankName);
        group.setBankAccount(bankAccount);
        group.setPaymentDay(paymentDay);
        group.setInviteCode(inviteCode);
        return group;
    }

    public static GroupEntity fromDomain(Group group) {
        return GroupEntity.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .subId(group.getSubId())
                .leaderId(group.getLeaderId())
                .memberIds(group.getMemberIds())
                .bankName(group.getBankName())
                .bankAccount(group.getBankAccount())
                .paymentDay(group.getPaymentDay())
                .inviteCode(group.getInviteCode())
                .build();
    }
}
