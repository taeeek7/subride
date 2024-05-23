package com.subride.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDTO<T> {
    private int code;
    private String message;
    private T response;
}
