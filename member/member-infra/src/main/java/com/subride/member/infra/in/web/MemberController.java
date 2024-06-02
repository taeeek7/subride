package com.subride.member.infra.in.web;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@SecurityRequirement(name = "bearerAuth")   //이 어노테이션이 없으면 요청 헤더에 Authorization헤더가 안 생김
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "MemberEntity API", description = "회원 관련 API")
public class MemberController {
    private final MemberControllerHelper memberControllerHelper;

    @GetMapping("/{userId}")
    @Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회한다.")
    public ResponseEntity<ResponseDTO<MemberInfoDTO>> getMemberInfo(@PathVariable String userId) {
        MemberInfoDTO memberInfoDTO = memberControllerHelper.getMemberInfo(userId);
        return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "회원정보", memberInfoDTO));

    }

    @GetMapping
    @Operation(summary = "회원 정보 리스트 조회", description = "여러 회원의 정보를 조회한다.")
    @Parameters({
            @Parameter(name = "userIds", in = ParameterIn.QUERY, description = "사용자ID(콤마로 구분)", required = true)
    })
    public ResponseEntity<ResponseDTO<List<MemberInfoDTO>>> getMemberInfoList(@RequestParam String userIds) {
        List<String> userIdList = Arrays.asList(userIds.replaceAll("\\s", "").split(","));
        List<MemberInfoDTO> memberInfoDTOList = memberControllerHelper.getMemberInfoList(userIdList);
        return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "회원정보 리스트", memberInfoDTOList));

    }

}
