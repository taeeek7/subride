package com.subride.mygrp.biz.usecase.service;

import com.subride.mygrp.biz.domain.MyGroup;
import com.subride.mygrp.biz.dto.MyGroupCreateDTO;
import com.subride.mygrp.biz.dto.MyGroupDetailDTO;
import com.subride.mygrp.biz.dto.MyGroupJoinDTO;
import com.subride.mygrp.biz.dto.MyGroupSummaryDTO;
import com.subride.mygrp.biz.exception.BizException;
import com.subride.mygrp.biz.usecase.inport.IMyGroupService;
import com.subride.mygrp.biz.usecase.outport.IMyGroupProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyGroupServiceImpl implements IMyGroupService {
    private final IMyGroupProvider myGroupProvider;

    @Override
    public List<MyGroupSummaryDTO> getMyGroupSummaryList(String userId) {
        List<MyGroup> myGroupList = myGroupProvider.getMyGroupListByUserId(userId);
        return myGroupList.stream()
                .map(this::toMyGroupSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MyGroupDetailDTO getMyGroupDetail(Long myGroupId) {
        MyGroup myGroup = myGroupProvider.getMyGroupByMyGroupId(myGroupId);
        return toMyGroupDetailDTO(myGroup);
    }

    @Override
    public void createMyGroup(MyGroupCreateDTO myGroupCreateDTO) {
        MyGroup myGroup = toMyGroup(myGroupCreateDTO);
        myGroupProvider.saveMyGroup(myGroup);
    }

    @Override
    public void joinMyGroup(MyGroupJoinDTO myGroupJoinDTO) {
        MyGroup myGroup = myGroupProvider.getMyGroupByInviteCode(myGroupJoinDTO.getInviteCode());

        if (myGroup.getMemberIds().size() >= myGroup.getMaxMemberCount()) {
            throw new BizException("The group is already full.");
        }

        myGroup.getMemberIds().add(myGroupJoinDTO.getUserId());
        myGroupProvider.saveMyGroup(myGroup);

        // 사용자가 그룹의 구독서비스에 가입되어 있지 않으면 구독서비스 가입 처리
        if (!myGroupProvider.isSubscribed(myGroupJoinDTO.getUserId(), myGroup.getSubId())) {
            myGroupProvider.subscribeSub(myGroup.getSubId(), myGroupJoinDTO.getUserId());
        }
    }

    @Override
    public void leaveMyGroup(Long myGroupId, String userId) {
        if (!myGroupProvider.existsByMyGroupIdAndUserId(myGroupId, userId)) {
            throw new BizException("You are not a member of this group.");
        }
        myGroupProvider.deleteMyGroupUser(myGroupId, userId);
    }

    @Override
    public Long getTotalSubscriptionAmount(String userId) {
        return myGroupProvider.calculateTotalSubscriptionAmount(userId);
    }

    @Override
    public Long getMaxDiscountAmount(String userId) {
        return myGroupProvider.calculateMaxDiscountAmount(userId);
    }

    private MyGroupSummaryDTO toMyGroupSummaryDTO(MyGroup myGroup) {
        // ... mapping logic ...
        MyGroupSummaryDTO myGroupSummaryDTO = new MyGroupSummaryDTO();
        return myGroupSummaryDTO;
    }

    private MyGroupDetailDTO toMyGroupDetailDTO(MyGroup myGroup) {
        // ... mapping logic ...
        MyGroupDetailDTO myGroupDetailDTO = new MyGroupDetailDTO();
        return myGroupDetailDTO;
    }

    private MyGroup toMyGroup(MyGroupCreateDTO myGroupCreateDTO) {
        // ... mapping logic ...
        MyGroup myGroup = new MyGroup();
        return myGroup;
    }
}