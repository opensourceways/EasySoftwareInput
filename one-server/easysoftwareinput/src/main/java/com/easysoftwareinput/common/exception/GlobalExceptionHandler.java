package com.easysoftwareinput.common.exception;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger =  LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // @ExceptionHandler(HttpClientErrorException.class)
    // @ResponseStatus(HttpStatus.UNAUTHORIZED)
    // public ResponseEntity<Object> exception(HttpClientErrorException e) {
    //     // return ResultUtil.fail(HttpStatus.INTERNAL_SERVER_ERROR, MessageCode.ES0001, e.getMessage());
    // }
}
