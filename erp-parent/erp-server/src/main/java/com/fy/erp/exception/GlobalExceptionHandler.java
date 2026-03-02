package com.fy.erp.exception;

import com.fy.erp.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException ex) {
        int status = ex.getCode() >= 100 && ex.getCode() <= 599 ? ex.getCode() : 400;
        return ResponseEntity.status(status).body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().isEmpty()
                ? "参数校验失败"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(400).body(Result.fail(400, msg));
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
        return ResponseEntity.status(400).body(Result.fail(400, ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("不可读的 HTTP 消息或 JSON 格式错误: {}", ex.getMessage());
        return ResponseEntity.status(400).body(Result.fail(400, "请求参数格式或 JSON 结构错误，请检查"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("未处理异常: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(Result.fail(500, "服务器内部错误，请稍后重试"));
    }
}
