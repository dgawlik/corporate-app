package org.dgawlik.controller;

import org.dgawlik.exception.IllegalApiUseException;
import org.dgawlik.exception.NonExistingResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

public abstract class ApiBase {

    @ExceptionHandler(value
            = {IllegalApiUseException.class})
    public String handleIllegalApiUse(
            RuntimeException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ex.getMessage();
    }

    @ExceptionHandler(value
            = {NonExistingResourceException.class})
    public String handleNonExisting(
            RuntimeException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return ex.getMessage();
    }
}
