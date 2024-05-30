package com.subride.subrecommend.infra.out.feign;

import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mySubFeignClient", url = "${feign.mysub.url}")
public interface MySubFeignClient {
    @GetMapping("/api/my-subs/sub-id-list")
    ResponseDTO<List<Long>> getMySubIds(@RequestParam String userId);
}
