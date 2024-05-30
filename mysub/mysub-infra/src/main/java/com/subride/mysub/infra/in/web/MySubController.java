package com.subride.mysub.infra.in.web;

import com.subride.common.dto.MySubInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.mysub.biz.usecase.inport.IMySubService;
import com.subride.mysub.infra.exception.InfraException;
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
import java.util.stream.Collectors;

@Tag(name = "마이구독 서비스 API")
@RestController
@RequestMapping("/api/my-subs")
@SecurityRequirement(name = "bearerAuth")    //이 어노테이션이 없으면 요청 헤더에 Authorization헤더가 안 생김
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class MySubController {
    private final IMySubService mySubService;
    private final MySubControllerHelper mySubControllerHelper;

    @Operation(summary = "사용자 가입 구독서비스 목록 리턴", description = "구독추천 서비스에 요청하여 구독서비스 정보를 모두 담아 리턴함")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<MySubInfoDTO>>> getMySubList(@RequestParam String userId) {
        try {
            List<MySubInfoDTO> mySubInfoDTOList = mySubService.getMySubList(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 목록 조회 성공", mySubInfoDTOList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "가입 구독서비스 중 썹그룹에 참여하지 않은 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/not-join-group")
    public ResponseEntity<ResponseDTO<List<MySubInfoDTO>>> getNotJoinGroupSubList(@RequestParam String userId) {
        try {
            List<MySubInfoDTO> mySubInfoDTOList = mySubService.getMySubList(userId);
            List<MySubInfoDTO> notJoinGroupSubList = mySubInfoDTOList.stream()
                    .filter(mySubInfoDTO -> !mySubInfoDTO.isJoinGroup())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 목록 조회 성공", notJoinGroupSubList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독 취소", description = "구독서비스를 삭제합니다.")
    @Parameters({
            @Parameter(name = "subId", in = ParameterIn.PATH, description = "구독서비스 ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자 ID", required = true)
    })
    @DeleteMapping("/{subId}")
    public ResponseEntity<ResponseDTO<Void>> cancelSub(@PathVariable Long subId, @RequestParam String userId) {
        try {
            mySubService.cancelSub(subId, userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 취소 성공", null));
        }  catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독 등록", description = "구독서비스를 등록합니다.")
    @Parameters({
            @Parameter(name = "subId", in = ParameterIn.PATH, description = "구독서비스 ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자 ID", required = true)
    })
    @PostMapping("/{subId}")
    public ResponseEntity<ResponseDTO<Void>> subscribeSub(@PathVariable Long subId, @RequestParam String userId) {
        try {
            mySubService.subscribeSub(subId, userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 추가 성공", null));
        }  catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독여부 리턴", description = "사용자가 구독서비스를 가입했는지 여부를 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자 ID", required = true),
            @Parameter(name = "subId", in = ParameterIn.QUERY, description = "구독서비스 ID", required = true)
    })
    @GetMapping("/checking-subscribe")
    public ResponseEntity<ResponseDTO<Boolean>> checkSubscription(
            @RequestParam String userId,
            @RequestParam Long subId) {
        try {
            boolean isSubscribed = mySubService.checkSubscription(userId, subId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 여부 확인 성공", isSubscribed));
        }  catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "사용자의 가입 서비스ID 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/sub-id-list")
    public ResponseEntity<ResponseDTO<List<Long>>> getMySubIds(@RequestParam String userId) {
        try {
            List<Long> mySubIds = mySubControllerHelper.getMySubIds(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "사용자 가입 서비스 ID 리턴", mySubIds));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }
}