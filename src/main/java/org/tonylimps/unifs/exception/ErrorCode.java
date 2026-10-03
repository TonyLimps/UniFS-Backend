package org.tonylimps.unifs.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    LOGIN_FAILED(40001, HttpStatus.BAD_REQUEST, "Wrong user id or signature."),
    REGISTER_FAILED(40002, HttpStatus.BAD_REQUEST, "Wrong user id or signature."),
    JOIN_ROOM_FAILED(40003, HttpStatus.BAD_REQUEST, "Wrong room id or password."),
    ROOM_ACCESS_DENIED(40301, HttpStatus.FORBIDDEN, "You are not authorized to operate on this room."),
    MEMBER_ACCESS_DENIED(40304, HttpStatus.FORBIDDEN, "You are not authorized to operate on this member."),
    USER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "User not found."),
    ROOM_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "Room not found."),
    MEMBER_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "Member not found."),;

    final int code;
    final HttpStatus httpStatus;
    final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
