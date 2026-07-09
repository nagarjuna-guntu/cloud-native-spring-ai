package com.bookshop.order.tools;

import com.bookshop.order.order.domain.OrderService;
import com.bookshop.order.order.web.OrderResponse;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Component;

@Component
public class OrderMcpTools {

    private final OrderService orderService;

    public OrderMcpTools(OrderService orderService) {
        this.orderService = orderService;
    }

    @McpTool(name = "placeOrder", description = """
            Place an order for a book.
            The ISBN must be a valid book ISBN.
            If quantity is omitted use 1.
            
            Interpret the response as follows:
            
            - status=ACCEPTED means the order was successfully created.
            - status=REJECTED means the order could not be created.
            - reason explains why the order failed.
            - orderTotal is the final price for the requested quantity.
            - createdDate is when the order was created.
            
            Use this information to produce a friendly response.
            Do not invent additional details.
            """)
    public OrderResponse placeOrder(@McpToolParam (description = "The Book ISBN") String isbn,
                                    @McpToolParam(description = "The number of items to order (between 1 and 5)") int quantity) {
        return orderService.submitOrder(isbn, quantity);
    }
}
