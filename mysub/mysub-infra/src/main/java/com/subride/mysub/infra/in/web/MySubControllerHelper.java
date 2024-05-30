package com.subride.mysub.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.biz.dto.MySubDTO;
import com.subride.common.dto.MySubInfoDTO;
import com.subride.mysub.infra.out.feign.SubRecommendFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MySubControllerHelper {
    private final SubRecommendFeignClient subRecommendFeignClient;

    public MySubInfoDTO toMySubInfoDTO(MySubDTO mySubDTO) {
        MySubInfoDTO mySubInfoDTO = new MySubInfoDTO();
        mySubInfoDTO.setUserId(mySubDTO.getUserId());
        mySubInfoDTO.setSubId(mySubDTO.getSubId());

        ResponseDTO<SubInfoDTO> response = subRecommendFeignClient.getSubDetail(mySubDTO.getSubId());
        SubInfoDTO subInfoDTO = response.getResponse();

        mySubInfoDTO.setSubName(subInfoDTO.getSubName());
        mySubInfoDTO.setCategoryName(subInfoDTO.getCategoryName());
        mySubInfoDTO.setLogo(subInfoDTO.getLogo());
        mySubInfoDTO.setDescription(subInfoDTO.getDescription());
        mySubInfoDTO.setFee(subInfoDTO.getFee());
        mySubInfoDTO.setMaxShareNum(subInfoDTO.getMaxShareNum());

        return mySubInfoDTO;
    }

    public List<MySubInfoDTO> toMySubInfoDTOList(List<MySubDTO> mySubDTOList) {
        return mySubDTOList.stream()
                .map(this::toMySubInfoDTO)
                .collect(Collectors.toList());
    }
}