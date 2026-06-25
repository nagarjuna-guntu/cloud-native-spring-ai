package com.bookshop.edge.ordersummary;


public record Book(String isbn, String title,
                   String author, double price, String publisher) {
}
