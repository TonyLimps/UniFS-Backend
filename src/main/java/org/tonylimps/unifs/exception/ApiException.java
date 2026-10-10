package org.tonylimps.unifs.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final int code;
    private final HttpStatus httpStatus;

    public ApiException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        super(errorCode.getMessage());
    }
}
