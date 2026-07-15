package com.bookshop.order.tools;

import com.bookshop.order.order.domain.OrderService;
import com.bookshop.order.order.web.OrderRequest;
import com.bookshop.order.order.web.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderMcpTools {

    private final OrderService orderService;


    public OrderMcpTools(OrderService orderService) {
        this.orderService = orderService;
    }

    @McpTool(name = "placeOrder", description = """
            CRITICAL: You MUST call this tool to execute any book purchase or order placement.
            NEVER simulate, imagine, or invent an order response using your internal knowledge.
            You cannot verify price, inventory, or order status without executing this tool.
            
            Inputs:
            - isbn: A valid string representation of the book's ISBN.
            - quantity: The number of items to order (between 1 and 5). If omitted, default to 1.
            
            Interpret the returned JSON payload from the system directly to the user.
            """)
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        log.info("Agent invoked tool with flat properties mapped to object -> ISBN: {}, Qty: {}", orderRequest.isbn(), orderRequest.quantity());
        return orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }

    @McpTool(name = "findOrder", description = """
            Find an order by its ID.
            Inputs:
            - orderId: The ID of the order to find.
            """)
    public OrderResponse findOrder(@McpToolParam(description = "The ID of the order to find", required = true) Long orderId) {
        log.info("Agent invoked tool to find order with ID: {}", orderId);
        return orderService.findOrderById(orderId);
    }
}
