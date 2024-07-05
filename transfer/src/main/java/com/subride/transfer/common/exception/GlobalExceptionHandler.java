package com.subride.transfer.common.exception;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<ResponseDTO<Void>> handleInfraException(TransferException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
    }
}