package com.subride.common.util;

import com.subride.common.dto.ResponseDTO;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

    // 객체의 필드값 중 null 값이 있는지 체크하는 메서드
    public static <T> List<String> getNullFields(T object) {
        List<String> nullFields = new ArrayList<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.get(object) == null) {
                    nullFields.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace(); // 접근 불가한 경우 예외 처리
            }
        }
        return nullFields;
    }
}
