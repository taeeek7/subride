package com.subride.mygrp.infra.out.adapter;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mygrp.biz.domain.MyGroup;
import com.subride.mygrp.biz.usecase.outport.IMyGroupProvider;
import com.subride.mygrp.infra.exception.InfraException;
import com.subride.mygrp.infra.out.entity.MyGroupEntity;
import com.subride.mygrp.infra.out.feign.MySubFeignClient;
import com.subride.mygrp.infra.out.feign.SubRecommendFeignClient;
import com.subride.mygrp.infra.out.repo.IMyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyGroupProviderImpl implements IMyGroupProvider {
    private final IMyGroupRepository myGroupRepository;
    private final SubRecommendFeignClient subRecommendFeignClient;
    private final MySubFeignClient mySubFeignClient;

    @Override
    public MyGroup getMyGroupByMyGroupId(Long myGroupId) {
        MyGroupEntity myGroupEntity = myGroupRepository.findById(myGroupId)
                .orElseThrow(() -> new InfraException("My group not found"));
        return myGroupEntity.toDomain();
    }

    @Override
    public List<MyGroup> getMyGroupListByUserId(String userId) {
        List<MyGroupEntity> myGroupEntityList = myGroupRepository.findByMemberIdsContaining(userId);
        return myGroupEntityList.stream()
                .map(MyGroupEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByMyGroupIdAndUserId(Long myGroupId, String userId) {
        return myGroupRepository.existsByMyGroupIdAndMemberIdsContaining(myGroupId, userId);
    }

    @Override
    public void saveMyGroup(MyGroup myGroup) {
        MyGroupEntity myGroupEntity = MyGroupEntity.fromDomain(myGroup);
        myGroupRepository.save(myGroupEntity);
    }

    @Override
    public void deleteMyGroupUser(Long myGroupId, String userId) {
        MyGroupEntity myGroupEntity = myGroupRepository.findById(myGroupId)
                .orElseThrow(() -> new InfraException("My group not found"));

        myGroupEntity.getMemberIds().remove(userId);
        myGroupRepository.save(myGroupEntity);
    }

    @Override
    public Long calculateTotalSubscriptionAmount(String userId) {
        List<MyGroup> myGroupList = getMyGroupListByUserId(userId);
        return myGroupList.stream()
                .mapToLong(myGroup -> {
                    ResponseDTO<SubInfoDTO> response = subRecommendFeignClient.getSubDetail(myGroup.getSubId());
                    SubInfoDTO subInfoDTO = response.getResponse();
                    int memberCount = myGroup.getMemberIds().size();
                    return subInfoDTO.getFee() / memberCount;
                })
                .sum();
    }

    @Override
    public Long calculateMaxDiscountAmount(String userId) {
        List<MyGroup> myGroupList = getMyGroupListByUserId(userId);
        return myGroupList.stream()
                .mapToLong(myGroup -> {
                    ResponseDTO<SubInfoDTO> response = subRecommendFeignClient.getSubDetail(myGroup.getSubId());
                    SubInfoDTO subInfoDTO = response.getResponse();
                    int memberCount = myGroup.getMemberIds().size();
                    return subInfoDTO.getFee() - (subInfoDTO.getFee() / memberCount);
                })
                .sum();
    }

    @Override
    public MyGroup getMyGroupByInviteCode(String inviteCode) {
        MyGroupEntity myGroupEntity = myGroupRepository.findByInviteCode(inviteCode);
        if (myGroupEntity == null) {
            throw new InfraException("Invalid invite code");
        }
        return myGroupEntity.toDomain();
    }

    @Override
    public boolean isSubscribed(String userId, Long subId) {
        ResponseDTO<Boolean> response = mySubFeignClient.checkSubscription(userId, subId);
        return response.getResponse();
    }

    @Override
    public boolean subscribeSub(Long subId, String userId) {
        ResponseDTO<Void> response = mySubFeignClient.subscribeSub(subId, userId);
        return response.getCode() == 200;
    }
}