package com.cnmci.stats.controller;

import com.cnmci.stats.beans.MessageResponse;
import com.cnmci.stats.exception.OurGenericException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OurGenericException.class)
    public ResponseEntity<MessageResponse> bookNotFoundHandler(OurGenericException exception) {
        MessageResponse error = MessageResponse.builder()
                .id(0)
                .localDateTime(LocalDateTime.now())
                .message(exception.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(error);
    }
}
