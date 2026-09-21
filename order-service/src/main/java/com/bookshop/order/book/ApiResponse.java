package com.bookshop.order.book;

public sealed interface ApiResponse<T> permits Success, Failure {
    T getData();

    Throwable getError();
}
