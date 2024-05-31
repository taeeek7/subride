package com.subride.transfer.common.exception;

public class TransferException extends RuntimeException {
    private int code;

    public TransferException(String message) {
        super(message);
    }

    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferException(int code, String message) {
        super(message);
        this.code = code;
    }

    public TransferException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}