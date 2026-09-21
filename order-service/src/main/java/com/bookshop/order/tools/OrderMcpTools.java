package com.bookshop.order.tools;

import com.bookshop.order.order.domain.OrderService;
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

    @McpTool(name = "placeOrder",
            description = """
                            Places an order for a book.
                    
                            This tool to execute any book purchase or order placement.
                            NEVER simulate or invent an order result.
                    
                            Inputs:
                            - isbn: A valid string representation of the book's ISBN.
                            - quantity: The number of copies to order between 1 and 5.
                    
                            The returned response is the actual result of the order operation.
                    """,
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = false,
                    openWorldHint = false
            )
    )
    public OrderResponse placeOrder(@McpToolParam(description = "The ISBN of the book to order.",
                                            required = true) String isbn,
                                    @McpToolParam(description = "Number of copies to order, between 1 and 5.",
                                            required = true) Integer quantity) {
        log.info("Agent invoked tool with flat properties mapped to object -> ISBN: {}, Qty: {}", isbn, quantity);
        return orderService.submitOrder(isbn, quantity);
    }

    @McpTool(name = "findOrder",
            description = """
                        Finds an existing order by its numeric Order ID.
                    
                        IMPORTANT:
                        - orderId is a JSON NUMBER.
                        - orderId must be an integer.
                        - Do NOT pass orderId as a JSON string.
                        - Example of valid input: {"orderId": 30}
                        - Example of invalid input: {"orderId": "30"}
                    """,
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public OrderResponse findOrder(@McpToolParam(
            description = """
                    Numeric Order ID.
                    Must be an integer JSON number, not a string.
                    Example: 30, not "30".
                    """,
            required = true) Long orderId) {
        log.info("Agent invoked tool to find order with ID: {}", orderId);
        return orderService.findOrderById(orderId);
    }
}
