package com.subride.mygrp.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.mygrp.biz.dto.MyGroupCreateDTO;
import com.subride.mygrp.biz.dto.MyGroupDetailDTO;
import com.subride.mygrp.biz.dto.MyGroupJoinDTO;
import com.subride.mygrp.biz.dto.MyGroupSummaryDTO;
import com.subride.mygrp.biz.usecase.inport.IMyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "마이그룹 서비스 API")
@RestController
@SuppressWarnings("unused")
@RequestMapping("/api/my-groups")
@RequiredArgsConstructor
public class MyGroupController {
    private final IMyGroupService myGroupService;
    private final MyGroupControllerHelper controllerHelper;

    @Operation(summary = "사용자의 썹그룹 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<MyGroupSummaryDTO>>> getMyGroupList(@RequestParam String userId) {
        List<MyGroupSummaryDTO> myGroupSummaryDTOList = myGroupService.getMyGroupSummaryList(userId);
        return ResponseEntity.ok(ResponseDTO.<List<MyGroupSummaryDTO>>builder()
                .code(200)
                .message("마이그룹 목록 조회 성공")
                .response(myGroupSummaryDTOList)
                .build());
    }

    @Operation(summary = "썹그룹 상세 정보 리턴")
    @Parameters({
            @Parameter(name = "myGroupId", in = ParameterIn.QUERY, description = "썹그룹ID", required = true)
    })
    @GetMapping("/{myGroupId}")
    public ResponseEntity<ResponseDTO<MyGroupDetailDTO>> getMyGroupDetail(@PathVariable Long myGroupId) {
        MyGroupDetailDTO myGroupDetailDTO = myGroupService.getMyGroupDetail(myGroupId);
        return ResponseEntity.ok(ResponseDTO.<MyGroupDetailDTO>builder()
                .code(200)
                .message("마이그룹 상세 조회 성공")
                .response(myGroupDetailDTO)
                .build());
    }

    @Operation(summary = "썹그룹 생성", description = "새로운 썹그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<ResponseDTO<Void>> createMyGroup(@RequestBody MyGroupCreateDTO myGroupCreateDTO) {
        myGroupService.createMyGroup(myGroupCreateDTO);
        return ResponseEntity.ok(ResponseDTO.<Void>builder()
                .code(200)
                .message("마이그룹 생성 성공")
                .build());
    }

    @Operation(summary = "썹그룹 참여", description = "사용자ID와 썹그룹ID를 이용하여 썹그룹 참여정보 생성")
    @PostMapping("/join")
    public ResponseEntity<ResponseDTO<Void>> joinMyGroup(@RequestBody MyGroupJoinDTO myGroupJoinDTO) {
        myGroupService.joinMyGroup(myGroupJoinDTO);
        return ResponseEntity.ok(ResponseDTO.<Void>builder()
                .code(200)
                .message("마이그룹 참여 성공")
                .build());
    }

    @Operation(summary = "썹그룹 탈퇴", description = "썹그룹에서 탈퇴합니다.")
    @Parameters({
            @Parameter(name = "myGroupId", in = ParameterIn.PATH, description = "썹그룹ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @DeleteMapping("/{myGroupId}")
    public ResponseEntity<ResponseDTO<Void>> leaveMyGroup(@PathVariable Long myGroupId, @RequestParam String userId) {
        myGroupService.leaveMyGroup(myGroupId, userId);
        return ResponseEntity.ok(ResponseDTO.<Void>builder()
                .code(200)
                .message("마이그룹 탈퇴 성공")
                .build());
    }

    @Operation(summary = "월 총 구독료 리턴", description = "썹그룹 참여현황을 참조하여 매월 총 구독료를 계산합니다.")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/total-subscription-amount")
    public ResponseEntity<ResponseDTO<Long>> getTotalSubscriptionAmount(@RequestParam String userId) {
        Long totalSubscriptionAmount = myGroupService.getTotalSubscriptionAmount(userId);
        return ResponseEntity.ok(ResponseDTO.<Long>builder()
                .code(200)
                .message("총 구독료 조회 성공")
                .response(totalSubscriptionAmount)
                .build());
    }

    @Operation(summary = "월 최대 절감 가능액 리턴", description = "썹그룹 참여롤 통해 절약할 수 있는 최대액을 계산합니다.")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/max-discount-amount")
    public ResponseEntity<ResponseDTO<Long>> getMaxDiscountAmount(@RequestParam String userId) {
        Long maxDiscountAmount = myGroupService.getMaxDiscountAmount(userId);
        return ResponseEntity.ok(ResponseDTO.<Long>builder()
                .code(200)
                .message("최대 절감액 조회 성공")
                .response(maxDiscountAmount)
                .build());
    }
}