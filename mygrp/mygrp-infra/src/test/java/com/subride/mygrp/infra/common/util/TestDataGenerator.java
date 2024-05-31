// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/common/util/TestDataGenerator.java
package com.subride.mygrp.infra.common.util;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.infra.out.entity.GroupEntity;

import java.util.*;

public class TestDataGenerator {

    public static List<GroupEntity> generateGroupEntities() {
        List<GroupEntity> groupEntities = new ArrayList<>();

        GroupEntity group1 = GroupEntity.builder()
                .groupName("테스트그룹")
                .subId(1L)
                .leaderId("leader01")
                .memberIds(Set.of("user01", "user02", "user03"))
                .bankName("테스트은행")
                .bankAccount("1234-5678")
                .paymentDay(5)
                .inviteCode(generateRandomInviteCode())
                .build();

        GroupEntity group2 = GroupEntity.builder()
                .groupName("또다른그룹")
                .subId(2L)
                .leaderId("leader02")
                .memberIds(Set.of("user04", "user05"))
                .bankName("또다른은행")
                .bankAccount("9876-5432")
                .paymentDay(10)
                .inviteCode(generateRandomInviteCode())
                .build();

        groupEntities.add(group1);
        groupEntities.add(group2);

        return groupEntities;
    }

    public static GroupEntity generateGroupEntity() {
        return GroupEntity.builder()
                .groupName("새로운그룹")
                .subId(3L)
                .leaderId("newLeader")
                .memberIds(new HashSet<>())
                .bankName("새은행")
                .bankAccount("1111-2222")
                .paymentDay(15)
                .inviteCode(generateRandomInviteCode())
                .build();
    }

    public static GroupCreateDTO generateGroupCreateDTO() {
        GroupCreateDTO groupCreateDTO = new GroupCreateDTO();
        groupCreateDTO.setGroupName("새로운 그룹");
        groupCreateDTO.setSubId(1L);
        groupCreateDTO.setLeaderId("newLeader");
        groupCreateDTO.setBankName("새 은행");
        groupCreateDTO.setBankAccount("1234-5678");
        groupCreateDTO.setPaymentDay(5);
        return groupCreateDTO;
    }

    public static Group createGroup() {
        Group group = new Group();
        group.setGroupId(1L);
        group.setGroupName("썹그룹1");
        group.setSubId(100L);
        group.setLeaderId("user99");
        group.setMemberIds(new HashSet<>(Arrays.asList("user01", "user02")));
        group.setBankName("KB");
        group.setBankAccount("1223-222");
        group.setPaymentDay(7);
        group.setInviteCode("fd3dfds");
        group.setSubName("넷플릭스");
        group.setLogo("abc.png");
        group.setFee(15000L);
        group.setMaxShareNum(5);

        List<MemberInfoDTO> members = new ArrayList<>();
        MemberInfoDTO member = createMemberInfoDTO();
        members.add(member);
        group.setMembers(members);

        return group;
    }

    public static MemberInfoDTO createMemberInfoDTO() {
        MemberInfoDTO memberInfo = new MemberInfoDTO();
        memberInfo.setUserId("user01");
        memberInfo.setUserName("유저01");
        memberInfo.setBankName("KB");
        memberInfo.setBankAccount("123-1111");
        memberInfo.setCharacterId(1);
        return memberInfo;
    }

    public static <T> ResponseDTO<T> generateResponseDTO(int code, T response) {
        return ResponseDTO.<T>builder()
                .code(code)
                .response(response)
                .build();
    }

    public static String generateRandomInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}