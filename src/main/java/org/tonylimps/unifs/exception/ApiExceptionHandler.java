package org.tonylimps.unifs.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleException(ApiException ex) {
        var status = ex.getHttpStatus();
        if (status.is5xxServerError()) {
            log.error("Internal Server Error", ex);
        }
        return ResponseEntity.status(ex.getHttpStatus()).body(ex.getCode()+" "+ex.getMessage());
    }
}
