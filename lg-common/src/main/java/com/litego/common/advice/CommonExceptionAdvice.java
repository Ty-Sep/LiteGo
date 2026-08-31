package com.litego.common.advice;

import com.litego.common.domain.R;
import com.litego.common.exception.BadRequestException;
import com.litego.common.exception.CommonException;
import com.litego.common.exception.DbException;
import com.litego.common.utils.WebUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {

    /**
     * 处理客户端主动断开连接导致的 IOException
     * 避免在响应已提交后再次尝试写入 JSON，导致二次异常
     */
    @ExceptionHandler(IOException.class)
    public void handleClientAbort(IOException e, HttpServletResponse response) {
        String msg = e.getMessage();
        if (msg != null && (msg.contains("中止") || msg.contains("broken pipe") || msg.contains("Connection reset"))) {
            log.warn("客户端连接已中断，忽略异常: {}", msg);
            // 如果响应尚未提交，设置一个状态码（但无需返回 body）
            if (!response.isCommitted()) {
                try {
                    response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                } catch (Exception ignored) {
                }
            }
        } else {
            // 其他类型的 IOException 正常记录并尝试返回错误
            log.error("IO异常", e);
            if (!response.isCommitted()) {
                try {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"code\":500,\"msg\":\"IO错误\"}");
                } catch (IOException ignored) {
                }
            }
        }
    }

    @ExceptionHandler(DbException.class)
    public Object handleDbException(DbException e) {
        log.error("mysql数据库操作异常 -> ", e);
        return processResponse(e);
    }

    @ExceptionHandler(CommonException.class)
    public Object handleBadRequestException(CommonException e) {
        log.error("自定义异常 -> {} , 异常原因：{}  ", e.getClass().getName(), e.getMessage());
        log.debug("", e);
        return processResponse(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("|"));
        log.error("请求参数校验异常 -> {}", msg);
        log.debug("", e);
        return processResponse(new BadRequestException(msg));
    }

    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e) {
        log.error("请求参数绑定异常 ->BindException， {}", e.getMessage());
        log.debug("", e);
        return processResponse(new BadRequestException("请求参数格式错误"));
    }

    /**
     * 处理 NestedServletException，检查根因是否为客户断开连接
     */
    @ExceptionHandler(NestedServletException.class)
    public Object handleNestedServletException(NestedServletException e, HttpServletResponse response) {
        Throwable rootCause = e.getRootCause();
        if (rootCause instanceof IOException) {
            // 委托给 IOException 处理方法，复用逻辑
            handleClientAbort((IOException) rootCause, response);
            return null;  // 该方法已处理响应，无需进一步返回
        }
        log.error("参数异常 -> NestedServletException，{}", e.getMessage());
        log.debug("", e);
        return processResponse(new BadRequestException("请求参数处理异常"));
    }

    @ExceptionHandler(Exception.class)
    public Object handleRuntimeException(Exception e) {
        log.error("其他异常 uri : {} -> ", Objects.requireNonNull(WebUtils.getRequest()).getRequestURI(), e);
        return processResponse(new CommonException("服务器内部异常", 500));
    }

    private ResponseEntity<R<Void>> processResponse(CommonException e) {
        return ResponseEntity.status(e.getCode()).body(R.error(e));
    }
}