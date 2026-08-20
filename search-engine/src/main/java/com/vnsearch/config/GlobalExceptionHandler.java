package com.vnsearch.config;

import com.vnsearch.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler { 
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MissingServletRequestParameterException.class) 
    public ResponseEntity<Map<String, Object>> handleMissingParam() {

    }

    @ExceptionHandler(MethodArgumentNotValidException.class) 

    

    @ExceptionHandler(ConstraintViolationException) 

    @ExceptionHandler(UserService.InvalidCredentialsException.class)

    @ExceptionHandler(UserService.AuthException.class)

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)

    @ExceptionHandler(IllegalArgumentException.class)

    @ExceptionHandler(Exception.class)

    private ResponseEntity<Map<String,Object>> errorResponse() {
        
    }


}