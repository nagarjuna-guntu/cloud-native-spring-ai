package com.bookshop.order.book;

public record Success<T>(T data) implements ApiResponse<T> {

    @Override
    public T getData() {
        return data;
    }

    @Override
    public Throwable getError() {
        throw new RuntimeException("Invalid Invocation");
    }
}
