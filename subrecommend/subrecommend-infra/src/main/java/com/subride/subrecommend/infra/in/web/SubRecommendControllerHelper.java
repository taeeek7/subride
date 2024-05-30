package com.subride.subrecommend.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.infra.exception.InfraException;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.feign.MySubFeignClient;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SubRecommendControllerHelper {
    private final ISubRepository subRepository;
    private final MySubFeignClient mySubFeignClient;

    public SubInfoDTO getSubDetail(Long subId) {
        SubEntity sub = subRepository.findById(subId)
                .orElseThrow(() -> new IllegalArgumentException("Sub not found"));
        return toSubInfoDTO(sub);
    }

    public SubInfoDTO toSubInfoDTO(SubEntity sub) {
        SubInfoDTO subInfoDTO = new SubInfoDTO();
        subInfoDTO.setId(sub.getId());
        subInfoDTO.setLogo(sub.getLogo());
        subInfoDTO.setName(sub.getName());
        subInfoDTO.setCategoryName(sub.getCategory().getCategoryName());
        subInfoDTO.setDescription(sub.getDescription());
        subInfoDTO.setFee(sub.getFee());
        subInfoDTO.setMaxShareNum(sub.getMaxShareNum());
        return subInfoDTO;
    }

    public List<SubInfoDTO> toSubInfoDTOList(List<SubEntity> subList) {
        return subList.stream()
                .map(this::toSubInfoDTO)
                .collect(Collectors.toList());
    }

    public List<SubInfoDTO> getNonSubList(List<SubInfoDTO> subList, String userId) {
        ResponseDTO<List<Long>> response = mySubFeignClient.getMySubIds(userId);
        if(response.getCode()==0) {
            throw new InfraException(0, "구독ID 리스트 구하기 실패");
        }
        List<Long> mySubIds = response.getResponse();

        return subList.stream()
                .filter(subInfoDTO -> !mySubIds.contains(subInfoDTO.getId()))
                .collect(Collectors.toList());
    }
}