package com.subride.subrecommend.infra.in.web;

import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SubRecommendControllerHelper {
    private final ISubRepository subRepository;

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
}