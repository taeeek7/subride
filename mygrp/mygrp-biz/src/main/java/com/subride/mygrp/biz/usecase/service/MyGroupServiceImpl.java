package com.subride.mygrp.biz.usecase.service;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.biz.usecase.inport.IMyGroupService;
import com.subride.mygrp.biz.usecase.outport.IMyGroupProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyGroupServiceImpl implements IMyGroupService {
    private final IMyGroupProvider myGroupProvider;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public List<Group> getMyGroupSummaryList(String userId) {
        List<Group> myGroupList = myGroupProvider.getMyGroupListByUserId(userId);
        return myGroupList;
    }

    @Override
    public Group getMyGroupDetail(Long myGroupId, String userId) {
        return myGroupProvider.getMyGroupByGroupId(myGroupId, userId);
    }

    @Override
    public void createMyGroup(GroupCreateDTO groupCreateDTO) {
        Group myGroup = new Group();
        myGroup.setGroupName(groupCreateDTO.getGroupName());
        myGroup.setSubId(groupCreateDTO.getSubId());
        myGroup.setLeaderId(groupCreateDTO.getLeaderId());
        myGroup.setMemberIds(Collections.singleton(groupCreateDTO.getLeaderId()));
        myGroup.setBankName(groupCreateDTO.getBankName());
        myGroup.setBankAccount(groupCreateDTO.getBankAccount());
        myGroup.setPaymentDay(groupCreateDTO.getPaymentDay());
        myGroup.setInviteCode(randomValueGenerator.generateUniqueRandomValue());
        myGroup.setMaxShareNum(groupCreateDTO.getMaxShareNum());

        myGroupProvider.createMyGroup(myGroup);
    }

    @Override
    public void joinMyGroup(GroupJoinDTO groupJoinDTO) {
        myGroupProvider.joinMyGroup(groupJoinDTO);
    }

    @Override
    public void leaveMyGroup(Long myGroupId, String userId) {
        myGroupProvider.leaveMyGroup(myGroupId, userId);
    }
}