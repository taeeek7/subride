package com.subride.mysub.infra.out.adapter;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.MySubInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.biz.usecase.outport.IMySubProvider;
import com.subride.mysub.infra.exception.InfraException;
import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.feign.MyGroupFeignClient;
import com.subride.mysub.infra.out.feign.SubRecommendFeignClient;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MySubProviderImpl implements IMySubProvider {
    private final IMySubRepository mySubRepository;
    private final SubRecommendFeignClient subRecommendFeignClient;
    private final MyGroupFeignClient myGroupFeignClient;

    @Override
    public List<MySubInfoDTO> getMySubList(String userId) {
        List<MySubEntity> mySubEntityList = mySubRepository.findByUserId(userId);
        if (mySubEntityList.isEmpty()) {
            // userId에 해당하는 MySubEntity가 없는 경우 처리
            throw new InfraException(0, "해당 사용자의 구독 정보가 없습니다.");
        }

        List<Long> mySubIds = mySubEntityList.stream()
                .map(MySubEntity::getSubId)
                .collect(Collectors.toList());

        ResponseDTO<List<GroupSummaryDTO>> myGroupListResponse = myGroupFeignClient.getMyGroupList(userId);
        List<GroupSummaryDTO> myGroupList = myGroupListResponse.getResponse();

        ResponseDTO<List<SubInfoDTO>> response = subRecommendFeignClient.getSubInfoListByIds(mySubIds);
        List<SubInfoDTO> subInfoList = response.getResponse();

        return mySubEntityList.stream()
                .map(mySubEntity -> {
                    MySubInfoDTO mySubInfoDTO = new MySubInfoDTO();
                    mySubInfoDTO.setUserId(mySubEntity.getUserId());
                    mySubInfoDTO.setSubId(mySubEntity.getSubId());

                    //구독정보 찾기
                    SubInfoDTO subInfo = subInfoList.stream()
                            .filter(dto -> dto.getSubId().equals(mySubEntity.getSubId()))
                            .findFirst()
                            .orElse(null);

                    if(subInfo != null) {
                        mySubInfoDTO.setSubName(subInfo.getSubName());
                        mySubInfoDTO.setCategoryName(subInfo.getCategoryName());
                        mySubInfoDTO.setLogo(subInfo.getLogo());
                        mySubInfoDTO.setDescription(subInfo.getDescription());
                        mySubInfoDTO.setFee(subInfo.getFee());
                        mySubInfoDTO.setMaxShareNum(subInfo.getMaxShareNum());

                        // joinGroupMemberCountList에서 동일한 subId를 가진 객체 찾기
                        GroupSummaryDTO groupSummaryDTO = myGroupList.stream()
                                .filter(dto -> dto.getSubId().equals(mySubEntity.getSubId()))
                                .findFirst()
                                .orElse(null);

                        if (groupSummaryDTO != null) {
                            mySubInfoDTO.setJoinGroup(true);
                            mySubInfoDTO.setPayedFee(subInfo.getFee() / groupSummaryDTO.getMemberCount());
                            mySubInfoDTO.setDiscountedFee(0L);
                        } else {
                            mySubInfoDTO.setJoinGroup(false);
                            mySubInfoDTO.setPayedFee(subInfo.getFee());
                            mySubInfoDTO.setDiscountedFee(subInfo.getFee() - subInfo.getFee() / subInfo.getMaxShareNum());
                        }
                    }
                    return mySubInfoDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void cancelSub(Long subId, String userId) {
        MySubEntity mySubEntity = mySubRepository.findByUserIdAndSubId(userId, subId)
                .orElseThrow(() -> new InfraException("구독 정보가 없습니다."));

        ResponseDTO<List<Long>> response = myGroupFeignClient.getJoinSubIds(userId);
        List<Long> joinSubIds = response.getResponse();

        // joinGroupMemberCountList에서 해당 subId를 가진 그룹이 있는지 확인
        boolean isJoinedGroup = joinSubIds.contains(subId);

        if (isJoinedGroup) {
            throw new InfraException(0, "썹 그룹에 참여중인 서비스는 가입취소 할 수 없습니다.");
        }

        mySubRepository.delete(mySubEntity);
    }

    @Override
    public void subscribeSub(Long subId, String userId) {

        MySubEntity mySubEntity = MySubEntity.builder()
                .userId(userId)
                .subId(subId)
                .build();
        mySubRepository.save(mySubEntity);
    }

    @Override
    public boolean isSubscribed(String userId, Long subId) {
        return mySubRepository.existsByUserIdAndSubId(userId, subId);
    }
}