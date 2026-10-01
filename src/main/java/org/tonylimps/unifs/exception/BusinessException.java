package org.tonylimps.unifs.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final HttpStatus httpStatus;

    public BusinessException(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        super(message);
    }

    public BusinessException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        super(errorCode.getMessage());
    }
}
