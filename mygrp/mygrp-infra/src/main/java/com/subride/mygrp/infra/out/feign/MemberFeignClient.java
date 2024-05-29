package com.subride.mygrp.infra.out.feign;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "memberFeignClient", url = "${feign.member.url}")
public interface MemberFeignClient {
    @GetMapping("/api/members/{userId}")
    ResponseDTO<MemberInfoDTO> getMemberInfo(@PathVariable String userId);

    @GetMapping("/api/members")
    ResponseDTO<List<MemberInfoDTO>> getMemberInfoList(@RequestParam String userIds);
}
