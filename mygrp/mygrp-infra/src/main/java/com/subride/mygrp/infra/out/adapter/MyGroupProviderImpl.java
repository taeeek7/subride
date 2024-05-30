package com.subride.mygrp.infra.out.adapter;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.biz.usecase.outport.IMyGroupProvider;
import com.subride.mygrp.infra.exception.InfraException;
import com.subride.mygrp.infra.out.entity.GroupEntity;
import com.subride.mygrp.infra.out.feign.MemberFeignClient;
import com.subride.mygrp.infra.out.feign.MySubFeignClient;
import com.subride.mygrp.infra.out.feign.SubRecommendFeignClient;
import com.subride.mygrp.infra.out.repo.IMyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyGroupProviderImpl implements IMyGroupProvider {
    private final IMyGroupRepository myGroupRepository;
    private final SubRecommendFeignClient subRecommendFeignClient;
    private final MySubFeignClient mySubFeignClient;
    private final MemberFeignClient memberFeignClient;

    @Override
    public Group getMyGroupByGroupId(Long groupId, String userId) {
        GroupEntity groupEntity = myGroupRepository.findById(groupId)
                .orElseThrow(() -> new InfraException(0, "My group not found", new Exception("Can't find group with "+groupId)));

        // SubRecommendFeignClient를 사용하여 구독 정보 조회
        ResponseDTO<SubInfoDTO> responseSub = subRecommendFeignClient.getSubDetail(groupEntity.getSubId());
        SubInfoDTO subInfoDTO = responseSub.getResponse();

        // 그룹 엔티티와 구독 정보를 매핑하여 Group 도메인 객체 생성
        Group group = toGroup(groupEntity, subInfoDTO, userId);

        // MemberFeignClient를 사용하여 사용자 정보 조회
        String userIds = String.join(",", groupEntity.getMemberIds());
        ResponseDTO<List<MemberInfoDTO>> responseMember = memberFeignClient.getMemberInfoList(userIds);
        List<MemberInfoDTO> memberInfoDTOList = responseMember.getResponse();
        group.setMembers(memberInfoDTOList);

        return group;
    }

    @Override
    public List<Group> getMyGroupListByUserId(String userId) {
        List<GroupEntity> groupEntityList = myGroupRepository.findByMemberIdsContaining(userId);

        // 그룹 엔티티에서 구독 ID 리스트 추출
        List<Long> subIds = groupEntityList.stream()
                .map(GroupEntity::getSubId)
                .collect(Collectors.toList());

        // SubRecommendFeignClient를 사용하여 구독 정보 리스트 조회
        ResponseDTO<List<SubInfoDTO>> responseDTO = subRecommendFeignClient.getSubInfoListByIds(subIds);
        List<SubInfoDTO> subInfoDTOList = responseDTO.getResponse();

        // 그룹 엔티티와 구독 정보를 매핑하여 Group 도메인 객체 생성
        return groupEntityList.stream()
                .map(groupEntity -> {
                    SubInfoDTO subInfoDTO = subInfoDTOList.stream()
                            .filter(dto -> dto.getSubId().equals(groupEntity.getSubId()))
                            .findFirst()
                            .orElse(null);

                    return toGroup(groupEntity, subInfoDTO, userId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void createMyGroup(Group myGroup) {
        if(myGroupRepository.existsByGroupNameAndSubId(myGroup.getGroupName(), myGroup.getSubId())) {
            throw new InfraException(0, "이미 등록된 그룹입니다.");
        }
        GroupEntity groupEntity = GroupEntity.fromDomain(myGroup);
        myGroupRepository.save(groupEntity);
    }

    @Override
    @Transactional
    public void joinMyGroup(GroupJoinDTO groupJoinDTO) {
        GroupEntity groupEntity = myGroupRepository.findByInviteCode(groupJoinDTO.getInviteCode());
        if (groupEntity == null) {
            throw new InfraException(0, "초대코드에 해당하는 썹그룹이 없습니다.");
        }

        if (groupEntity.getMemberIds().contains(groupJoinDTO.getUserId())) {
            throw new InfraException(0, "이미 썹그룹에 참석하였습니다.");
        }

        if (groupEntity.getMemberIds().size() >= groupEntity.getMaxShareNum()) {
            throw new InfraException(0, "이미 멤버 구성이 완료되었습니다.");
        }

        groupEntity.getMemberIds().add(groupJoinDTO.getUserId());
        myGroupRepository.save(groupEntity);

        // 사용자가 그룹의 구독서비스에 가입되어 있지 않으면 구독서비스 가입 처리
        if (!isSubscribed(groupJoinDTO.getUserId(), groupEntity.getSubId())) {
            if(!subscribeSub(groupEntity.getSubId(), groupJoinDTO.getUserId())) {
                throw new InfraException(0, "구독서비스 가입 처리중 오류가 발생하였습니다.");
            }
        }
    }
    public boolean isSubscribed(String userId, Long subId) {
        ResponseDTO<Boolean> response = mySubFeignClient.checkSubscription(userId, subId);
        return response.getResponse();
    }
    public boolean subscribeSub(Long subId, String userId) {
        ResponseDTO<Void> response = mySubFeignClient.subscribeSub(subId, userId);
        return response.getCode() == 200;
    }

    @Override
    public void leaveMyGroup(Long groupId, String userId) {
        GroupEntity groupEntity = myGroupRepository.findById(groupId)
                .orElseThrow(() -> new InfraException(0, "썹 그룹을 찾을 수 없습니다."));

        groupEntity.getMemberIds().remove(userId);
        myGroupRepository.save(groupEntity);
    }

    private Group toGroup(GroupEntity groupEntity, SubInfoDTO subInfoDTO, String userId) {
        Group group = groupEntity.toDomain();

        if (subInfoDTO != null) {
            group.setSubName(subInfoDTO.getSubName());
            group.setLogo(subInfoDTO.getLogo());
            group.setFee(subInfoDTO.getFee());
            group.setMaxShareNum(subInfoDTO.getMaxShareNum());
        }

        if(userId != null) {    //userId값이 있으면 실제 구독료와 최대 절감액을 계산함
            group.calulatePayedFee(userId);
            group.calulateDiscountedFee(userId);
        }
        return group;
    }
}