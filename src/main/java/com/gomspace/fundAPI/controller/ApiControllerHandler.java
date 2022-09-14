package com.gomspace.fundAPI.controller;

import com.gomspace.fundAPI.exception.AccountException;
import com.gomspace.fundAPI.exception.TransferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

/**
 * Handle Application Errors (AccountException, TransferException , result in response code 400
 * haldle Errors like resource not found, result in response code 404
 *
 */
@ControllerAdvice
public class ApiControllerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiControllerHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFound(NoHandlerFoundException ex) {
        logger.error("Error resource Not found ", ex.getMessage());
        return getExceptionResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<ExceptionResponse> handleTransferException(TransferException ex) {
        logger.error("Error on transfer ", ex.getMessage());
        return getExceptionResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<ExceptionResponse> handleAccountException(AccountException ex) {
        logger.error("Error on account ", ex.getMessage());
        return getExceptionResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ExceptionResponse> getExceptionResponse(HttpStatus status, String message) {
        ExceptionResponse response = new ExceptionResponse();
        response.setErrorCode(status.getReasonPhrase());
        response.setErrorMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(response, status);
    }
}
