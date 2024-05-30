package com.subride.mygrp.infra.out.feign;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "subRecommendFeignClient", url = "${feign.subrecommend.url}")
public interface SubRecommendFeignClient {
    @GetMapping("/api/subrecommend/detail/{subId}")
    ResponseDTO<SubInfoDTO> getSubDetail(@PathVariable("subId") Long subId);

    @GetMapping("/api/subrecommend/list-by-ids")
    ResponseDTO<List<SubInfoDTO>> getSubInfoListByIds(@RequestParam List<Long> subIds);
}
