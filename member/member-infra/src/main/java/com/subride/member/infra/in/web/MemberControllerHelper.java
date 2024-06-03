package com.subride.member.infra.in.web;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.out.entity.MemberEntity;
import com.subride.member.infra.out.repo.IMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberControllerHelper {
    public final IMemberRepository memberRepository;

    public MemberInfoDTO getMemberInfo(String userId) {
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new InfraException(0, "사용자 없음"));

        MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
        BeanUtils.copyProperties(member, memberInfoDTO);
        return memberInfoDTO;
    }

    public List<MemberInfoDTO> getMemberInfoList(List<String> userIdList) {
        List<MemberEntity> memberList = memberRepository.findByUserIdIn(userIdList);

        if (memberList.isEmpty()) {
            throw new InfraException(0, "검색할 회원정보 없음");
        }

        List<MemberInfoDTO> memberInfoDTOList = new ArrayList<>();
        for (MemberEntity member : memberList) {
            MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
            BeanUtils.copyProperties(member, memberInfoDTO);
            memberInfoDTOList.add(memberInfoDTO);
        }

        return memberInfoDTOList;
    }
}
