package com.subride.mygrp.infra.in.web;

import com.subride.common.dto.GroupMemberDTO;
import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupDetailDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.biz.usecase.inport.IMyGroupService;
import com.subride.mygrp.infra.exception.InfraException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "마이그룹 서비스 API")
@RestController
@SuppressWarnings("unused")
@RequestMapping("/api/my-groups")
@SecurityRequirement(name = "bearerAuth")    //이 어노테이션이 없으면 요청 헤더에 Authorization헤더가 안 생김
@RequiredArgsConstructor
public class MyGroupController {
    private final IMyGroupService myGroupService;
    private final MyGroupControllerHelper myGroupControllerHelper;

    @Operation(summary = "사용자의 썹그룹 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<GroupSummaryDTO>>> getMyGroupList(@RequestParam String userId) {
        try {
            List<Group> groupGroupList = myGroupService.getMyGroupSummaryList(userId);

            List<GroupSummaryDTO> groupSummaryDTOList = myGroupControllerHelper.getGroupSummaryList(groupGroupList);

            return ResponseEntity.ok(ResponseDTO.<List<GroupSummaryDTO>>builder()
                    .code(200)
                    .message("마이그룹 목록 조회 성공")
                    .response(groupSummaryDTOList)
                    .build());

        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "썹그룹 상세 정보 리턴")
    @Parameters({
            @Parameter(name = "groupId", in = ParameterIn.PATH, description = "썹그룹ID", required = true)
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<ResponseDTO<GroupDetailDTO>> getMyGroupDetail(@PathVariable Long groupId) {

        try {
            Group group = myGroupService.getMyGroupDetail(groupId);
            GroupDetailDTO groupDetailDTO = myGroupControllerHelper.getGroupDetail(group);

            return ResponseEntity.ok(ResponseDTO.<GroupDetailDTO>builder()
                    .code(200)
                    .message("마이그룹 상세 조회 성공")
                    .response(groupDetailDTO)
                    .build());

        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "썹그룹 생성", description = "새로운 썹그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<ResponseDTO<String>> createMyGroup(@RequestBody GroupCreateDTO groupCreateDTO) {
        try {
            List<String> nullFields = CommonUtils.getNullFields(groupCreateDTO);
            if(!nullFields.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(0, "입력 데이터에 널값이 있음"));
            }
            String inviteCode = myGroupService.createMyGroup(groupCreateDTO);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "썹그룹 생성 완료", inviteCode));

        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "썹그룹 참여", description = "사용자ID와 썹그룹ID를 이용하여 썹그룹 참여정보 생성")
    @PostMapping("/join")
    public ResponseEntity<ResponseDTO<Void>> joinMyGroup(@RequestBody GroupJoinDTO groupJoinDTO) {
        try {
            myGroupService.joinMyGroup(groupJoinDTO);
            return ResponseEntity.ok(ResponseDTO.<Void>builder()
                    .code(200)
                    .message("마이그룹 참여 성공")
                    .build());

        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "썹그룹 탈퇴", description = "썹그룹에서 탈퇴합니다.")
    @Parameters({
            @Parameter(name = "myGroupId", in = ParameterIn.PATH, description = "썹그룹ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @DeleteMapping("/{myGroupId}")
    public ResponseEntity<ResponseDTO<Void>> leaveMyGroup(@PathVariable Long myGroupId, @RequestParam String userId) {
        try {
            myGroupService.leaveMyGroup(myGroupId, userId);
            return ResponseEntity.ok(ResponseDTO.<Void>builder()
                    .code(200)
                    .message("마이그룹 탈퇴 성공")
                    .build());
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "참여중인 썹그룹의 구독서비스ID 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/sub-id-list")
    public ResponseEntity<ResponseDTO<List<Long>>> getJoinSubIds(@RequestParam String userId) {
        try {
            List<Long> joinSubIds = myGroupControllerHelper.getJoinSubIds(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "참여중인 썹그룹의 구독서비스ID 목록", joinSubIds));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "모든 썹그룹 멤버 목록 리턴")
    @GetMapping("/all-members")
    public ResponseEntity<ResponseDTO<List<GroupMemberDTO>>> getAllGroupMembers() {
        try {
            List<GroupMemberDTO> allGroupMemberss = myGroupControllerHelper.getAllGroupMembers();
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "모든 썹그룹 멤버 목록", allGroupMemberss));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }
}