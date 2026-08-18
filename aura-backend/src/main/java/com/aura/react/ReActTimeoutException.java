package com.aura.react;

/**
 * ReAct 超时异常
 */
public class ReActTimeoutException extends RuntimeException {

    public ReActTimeoutException(String message) {
        super(message);
    }

    public ReActTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
