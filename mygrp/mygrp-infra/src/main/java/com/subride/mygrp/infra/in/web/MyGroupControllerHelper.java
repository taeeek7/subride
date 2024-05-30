package com.subride.mygrp.infra.in.web;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupDetailDTO;
import com.subride.mygrp.infra.out.entity.GroupEntity;
import com.subride.mygrp.infra.out.repo.IMyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyGroupControllerHelper {
    private final IMyGroupRepository myGroupRepository;

    public List<GroupSummaryDTO> getGroupSummaryList(List<Group> myGroupList) {
        return myGroupList.stream()
                .map(this::toGroupSummaryDTO)
                .collect(Collectors.toList());
    }

    public GroupDetailDTO getGroupDetail(Group group) {
        GroupDetailDTO groupDetailDTO = new GroupDetailDTO();
        BeanUtils.copyProperties(group, groupDetailDTO);
        return groupDetailDTO;
    }

    public List<Long> getJoinSubIds(String userId) {
        List<GroupEntity> groupEntityList = myGroupRepository.findByMemberIdsContaining(userId);

        // 그룹 엔티티에서 그룹 참여 인원 정보 리스트 추출
        return groupEntityList.stream()
                .map(GroupEntity::getSubId)
                .collect(Collectors.toList());

    }

    private GroupSummaryDTO toGroupSummaryDTO(Group myGroup) {
        GroupSummaryDTO groupSummaryDTO = new GroupSummaryDTO();
        groupSummaryDTO.setGroupId(myGroup.getGroupId());
        groupSummaryDTO.setGroupName(myGroup.getGroupName());
        groupSummaryDTO.setSubId(myGroup.getSubId());
        groupSummaryDTO.setSubName(myGroup.getSubName());
        groupSummaryDTO.setLogo(myGroup.getLogo());
        groupSummaryDTO.setPaymentDay(myGroup.getPaymentDay());
        groupSummaryDTO.setFee(myGroup.getFee());
        groupSummaryDTO.setMemberCount(myGroup.getMemberIds().size());
        return groupSummaryDTO;
    }

}