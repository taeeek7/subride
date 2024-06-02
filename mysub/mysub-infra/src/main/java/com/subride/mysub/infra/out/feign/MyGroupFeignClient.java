package com.subride.mysub.infra.out.feign;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "myGroupFeignClient", url = "${feign.mygroup.url}")
public interface MyGroupFeignClient {
    @GetMapping("/api/my-groups")
    ResponseDTO<List<GroupSummaryDTO>> getMyGroupList(@RequestParam String userId);
    @GetMapping("/api/my-groups/sub-id-list")
    ResponseDTO<List<Long>> getJoinSubIds(@RequestParam String userId);
}
