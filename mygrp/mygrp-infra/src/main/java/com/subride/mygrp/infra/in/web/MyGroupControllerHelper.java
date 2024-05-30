package com.subride.mygrp.infra.in.web;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupDetailDTO;
import com.subride.mygrp.biz.dto.GroupSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyGroupControllerHelper {

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

    private GroupSummaryDTO toGroupSummaryDTO(Group myGroup) {
        GroupSummaryDTO groupSummaryDTO = new GroupSummaryDTO();
        groupSummaryDTO.setGroupId(myGroup.getGroupId());
        groupSummaryDTO.setGroupName(myGroup.getGroupName());
        groupSummaryDTO.setSubName(myGroup.getSubName());
        groupSummaryDTO.setLogo(myGroup.getLogo());
        groupSummaryDTO.setPaymentDay(myGroup.getPaymentDay());
        groupSummaryDTO.setFee(myGroup.getFee());
        groupSummaryDTO.setPayedFee(myGroup.getPayedFee());
        groupSummaryDTO.setDiscountedFee(myGroup.getDiscountedFee());
        return groupSummaryDTO;
    }
}