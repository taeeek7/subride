package com.subride.mygrp.infra.in.web;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mygrp.biz.domain.MyGroup;
import com.subride.mygrp.biz.dto.MyGroupCreateDTO;
import com.subride.mygrp.biz.dto.MyGroupDetailDTO;
import com.subride.mygrp.biz.dto.MyGroupSummaryDTO;
import com.subride.mygrp.infra.out.feign.MemberFeignClient;
import com.subride.mygrp.infra.out.feign.SubRecommendFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyGroupControllerHelper {
    private final SubRecommendFeignClient subRecommendFeignClient;
    private final MemberFeignClient memberFeignClient;

    public MyGroup toMyGroup(MyGroupCreateDTO myGroupCreateDTO) {
        MyGroup myGroup = new MyGroup();
        myGroup.setMyGroupName(myGroupCreateDTO.getMyGroupName());
        myGroup.setSubId(myGroupCreateDTO.getSubId());
        myGroup.setLeaderId(myGroupCreateDTO.getLeaderId());
        myGroup.setBankName(myGroupCreateDTO.getBankName());
        myGroup.setBankAccount(myGroupCreateDTO.getBankAccount());
        myGroup.setPaymentDay(myGroupCreateDTO.getPaymentDay());
        return myGroup;
    }

    public List<MyGroupSummaryDTO> toMyGroupSummaryDTOList(List<MyGroup> myGroupList) {
        return myGroupList.stream()
                .map(this::toMyGroupSummaryDTO)
                .collect(Collectors.toList());
    }

    public MyGroupSummaryDTO toMyGroupSummaryDTO(MyGroup myGroup) {
        MyGroupSummaryDTO myGroupSummaryDTO = new MyGroupSummaryDTO();
        myGroupSummaryDTO.setMyGroupId(myGroup.getMyGroupId());
        myGroupSummaryDTO.setMyGroupName(myGroup.getMyGroupName());

        ResponseDTO<SubInfoDTO> response = subRecommendFeignClient.getSubDetail(myGroup.getSubId());
        SubInfoDTO subInfoDTO = response.getResponse();
        myGroupSummaryDTO.setSubName(subInfoDTO.getSubName());
        myGroupSummaryDTO.setLogo(subInfoDTO.getLogo());
        myGroupSummaryDTO.setFee(subInfoDTO.getFee());

        int memberCount = myGroup.getMemberIds().size();
        myGroupSummaryDTO.setDiscountedFee(subInfoDTO.getFee() / memberCount);

        myGroupSummaryDTO.setPaymentDay(myGroup.getPaymentDay());
        return myGroupSummaryDTO;
    }

    public MyGroupDetailDTO toMyGroupDetailDTO(MyGroup myGroup) {
        MyGroupDetailDTO myGroupDetailDTO = new MyGroupDetailDTO();
        myGroupDetailDTO.setMyGroupId(myGroup.getMyGroupId());
        myGroupDetailDTO.setMyGroupName(myGroup.getMyGroupName());
        myGroupDetailDTO.setInviteCode(myGroup.getInviteCode());
        myGroupDetailDTO.setPaymentDay(myGroup.getPaymentDay());

        ResponseDTO<SubInfoDTO> responseSub = subRecommendFeignClient.getSubDetail(myGroup.getSubId());
        SubInfoDTO subInfoDTO = responseSub.getResponse();
        myGroupDetailDTO.setMaxMemberCount(subInfoDTO.getMaxShareNum());

        ResponseDTO<MemberInfoDTO> responseMember = memberFeignClient.getMemberInfo(myGroup.getLeaderId());
        MemberInfoDTO leaderInfoDTO = responseMember.getResponse();
        myGroupDetailDTO.setLeaderInfo(leaderInfoDTO);
        myGroupDetailDTO.setBankName(myGroup.getBankName());
        myGroupDetailDTO.setBankAccount(myGroup.getBankAccount());

        String memberIds = String.join(",", myGroup.getMemberIds());;
        ResponseDTO<List<MemberInfoDTO>> responseMembers = memberFeignClient.getMemberInfoList(memberIds);
        List<MemberInfoDTO> memberInfoDTOList = responseMembers.getResponse();
        myGroupDetailDTO.setMembers(memberInfoDTOList);
        myGroupDetailDTO.setMemberCount(memberInfoDTOList.size());

        return myGroupDetailDTO;
    }
}