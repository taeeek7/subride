package com.subride.subrecommend.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.biz.usecase.inport.ISubRecommendService;
import com.subride.subrecommend.infra.exception.InfraException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SubRecommend API", description = "구독추천 서비스 API")
@Slf4j
@RestController
@SecurityRequirement(name = "bearerAuth")    //이 어노테이션이 없으면 요청 헤더에 Authorization헤더가 안 생김
@RequestMapping("/api/subrecommend")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SubRecommendController {
    private final ISubRecommendService subRecommendService;
    private final SubRecommendControllerHelper subRecommendControllerHelper;

    @Operation(summary = "사용자 소비에 맞는 구독 카테고리 구하기",
            description = "최고 소비 카테고리와 매핑된 구독 카테고리의 Id, 이름, 소비액 총합을 리턴함")
    @Parameters({
      @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/category")
    public ResponseEntity<ResponseDTO<CategoryInfoDTO>> getRecommendCategory(@RequestParam String userId) {
        try {
            CategoryInfoDTO categoryInfoDTO = subRecommendService.getRecommendCategoryBySpending(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "소비성향에 맞는 구독 카테고리 리턴", categoryInfoDTO));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독 서비스 리스트 리턴", description = "카테고리ID에 해당하는 구독서비스 정보 리턴")
    @Parameters({
        @Parameter(name = "categoryId", in = ParameterIn.QUERY, description = "카테고리ID", required = true)
    })
    @GetMapping("/list")
    public ResponseEntity<ResponseDTO<List<SubInfoDTO>>> getRecommendSubList(@RequestParam Long categoryId) {
        try {
            List<SubInfoDTO> subInfoDTOList = subRecommendService.getRecommendSubListByCategory(categoryId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독서비스 리턴", subInfoDTOList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독상세 정보 리턴", description = "구독서비스ID에 해당하는 구독서비스 정보 리턴")
    @Parameters({
        @Parameter(name = "subId", in = ParameterIn.PATH, description = "구독서비스ID", required = true)
    })
    @GetMapping("/detail/{subId}")
    public ResponseEntity<ResponseDTO<SubInfoDTO>> getSubDetail(@PathVariable Long subId) {
        try {
            SubInfoDTO subInfoDTO = subRecommendControllerHelper.getSubDetail(subId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독상세 정보 리턴", subInfoDTO));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }

    }
}