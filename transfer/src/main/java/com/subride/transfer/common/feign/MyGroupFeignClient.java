package com.subride.transfer.common.feign;

import com.subride.common.dto.GroupMemberDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "myGroupFeignClient", url = "${feign.mygroup.url}")
public interface MyGroupFeignClient {
    @GetMapping("/api/my-groups/all-members")
    ResponseDTO<List<GroupMemberDTO>> getAllGroupMembers();
}
