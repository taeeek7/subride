package com.subride.mysub.infra.out.feign;

import com.subride.common.dto.ResponseDTO;
import com.subride.mysub.infra.dto.SubInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "subRecommendFeignClient", url = "${feign.subrecommend.url}")
public interface SubRecommendFeignClient {
    @GetMapping("/api/subrecommend/detail/{subId}")
    ResponseDTO<SubInfoDTO> getSubDetail(@PathVariable("subId") Long subId);
}
