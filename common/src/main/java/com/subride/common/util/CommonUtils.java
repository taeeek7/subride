package com.subride.common.util;

import com.subride.common.dto.*;

public class CommonUtils {

    //controller에서 성공 시 리턴 객체 반환
    public static <T> ResponseDTO<T> createSuccessResponse(int code, String message, T response) {
        return ResponseDTO.<T>builder()
                .code(code)
                .message(message)
                .response(response)
                .build();
    }

    //controller에서 실패 시 리턴 객체 반환
    public static <T> ResponseDTO<T> createFailureResponse(int code, String message) {
        return ResponseDTO.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
