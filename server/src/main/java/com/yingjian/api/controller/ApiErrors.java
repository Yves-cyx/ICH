package com.yingjian.api.controller;

import com.yingjian.api.model.Models.Envelope;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Envelope<Void>> invalid(MethodArgumentNotValidException error) {
        var field = error.getBindingResult().getFieldError();
        return fail(400, field == null ? "请检查输入内容" : field.getDefaultMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<Envelope<Void>> malformed(Exception error) { return fail(400, "请求参数无效"); }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Envelope<Void>> business(ResponseStatusException error) {
        return fail(error.getStatusCode().value(), error.getReason());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Envelope<Void>> missing(Exception error) { return fail(404, "找不到该内容"); }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Envelope<Void>> wrongMethod(HttpRequestMethodNotSupportedException error) {
        return ResponseEntity.status(405).headers(error.getHeaders()).body(Envelope.error(405, "请求方法不受支持"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<Envelope<Void>> wrongMedia(Exception error) { return fail(415, "请使用 JSON 提交数据"); }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Envelope<Void>> duplicate(Exception error) { return fail(409, "该账号已注册"); }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Envelope<Void>> unexpected(Exception error) {
        LoggerFactory.getLogger(ApiErrors.class).error("Unhandled API failure", error);
        return fail(500, "服务暂时不可用，请稍后再试");
    }

    private ResponseEntity<Envelope<Void>> fail(int status, String message) {
        return ResponseEntity.status(status).body(Envelope.error(status, message));
    }
}
