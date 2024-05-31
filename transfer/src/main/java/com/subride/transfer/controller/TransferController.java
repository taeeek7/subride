package com.subride.transfer.controller;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.common.exception.TransferException;
import com.subride.transfer.service.TransferService;
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

@Tag(name = "이체조회 서비스 API")
@RestController
@RequestMapping("/api/transfer")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @Operation(summary = "이체내역 조회", description = "특정 그룹의 이체내역을 조회합니다.")
    @Parameters({
            @Parameter(name = "groupId", in = ParameterIn.QUERY, description = "그룹 ID", required = true),
            @Parameter(name = "period", in = ParameterIn.QUERY, description = "조회 기간 (THREE_MONTHS, ONE_YEAR)", required = true)
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<TransferResponse>>> getTransferHistory(
            @RequestParam Long groupId,
            @RequestParam Period period) {
        try {
            List<TransferResponse> transferHistory = transferService.getTransferHistory(groupId, period);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "이체내역 조회 성공", transferHistory));
        } catch (TransferException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "테스트 데이터 생성", description = "테스트를 위한 데이터를 생성합니다.")
    @PostMapping("/test-data")
    public ResponseEntity<ResponseDTO<Void>> createTestData() {
        try {
            transferService.createTestData();
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "테스트 데이터 생성 성공", null));
        } catch (TransferException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "전체 데이터 삭제", description = "모든 이체 데이터를 삭제합니다.")
    @DeleteMapping("/all")
    public ResponseEntity<ResponseDTO<Void>> deleteAllData() {
        try {
            transferService.deleteAllData();
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "전체 데이터 삭제 성공", null));
        } catch (TransferException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }
}