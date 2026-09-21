package com.bookshop.agent.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalAgentExceptionHandler {

    @ExceptionHandler(JacksonException.class)
    public ProblemDetail jacksonException(JacksonException jacksonException) {
        log.error("AI Agent Tool threw exception.", jacksonException);
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, jacksonException.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public ProblemDetail throwable(Throwable throwable) {
        log.error("AI Agent Tool threw exception.", throwable);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
    }

    @ExceptionHandler(ToolExecutionException.class)
    public ProblemDetail toolExecutionException(ToolExecutionException toolExecutionException) {
        String toolName = toolExecutionException.getToolDefinition().name();
        log.error("AI Agent Tool [{}] threw exception.", toolName, toolExecutionException);

        var problemDetail = switch (toolExecutionException.getCause()) {
            case IllegalArgumentException illegalArgumentException ->
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, illegalArgumentException.getMessage());
            case IllegalStateException illegalStateException ->
                    ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, illegalStateException.getMessage());
            case Throwable throwable ->
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
        };

        // 3.diagnostic tracking metadata keys natively
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("timestamp", Instant.now());
        metadata.put("failingTool", toolName);

        problemDetail.setProperties(metadata);

        return problemDetail;
    }
}

