package com.subride.mygrp.infra.out.feign;

import com.subride.common.dto.MySubInfoDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mySubFeignClient", url = "${feign.mysub.url}")
public interface MySubFeignClient {
    @GetMapping("/api/my-subs")
    ResponseDTO<List<MySubInfoDTO>> getMySubList(@RequestParam String userId);

    @GetMapping("/api/my-subs/checking-subscribe")
    ResponseDTO<Boolean> checkSubscription(@RequestParam String userId, @RequestParam Long subId);

    @PostMapping("/api/my-subs/{subId}")
    ResponseDTO<Void> subscribeSub(@PathVariable Long subId, @RequestParam String userId);
}
