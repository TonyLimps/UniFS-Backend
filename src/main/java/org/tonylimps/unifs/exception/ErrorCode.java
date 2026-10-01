package org.tonylimps.unifs.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    LOGIN_FAILED(40001, HttpStatus.BAD_REQUEST, "Wrong user id or signature."),
    REGISTER_FAILED(40002, HttpStatus.BAD_REQUEST, "Wrong user id or signature."),
    USER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "User not found."),;

    final int code;
    final HttpStatus httpStatus;
    final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
