package com.subride.subrecommend.infra.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class InfraException extends RuntimeException {
    private int code;

    public InfraException(String message) {
        super(message);
    }

    public InfraException(String message, Throwable cause) {
        super(message, cause);
    }

    public InfraException(int code, String message) {
        super(message);
        this.code = code;
    }
    public InfraException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
