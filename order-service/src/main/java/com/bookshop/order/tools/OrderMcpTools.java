package com.bookshop.order.tools;

import com.bookshop.order.order.domain.OrderService;
import com.bookshop.order.order.web.OrderRequest;
import com.bookshop.order.order.web.OrderResponse;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class OrderMcpTools {

    private final OrderService orderService;

    public OrderMcpTools(OrderService orderService) {
        this.orderService = orderService;
    }

    @McpTool(name = "createOrder", description = """
            Places a new order. Requires the ISBN and Quantity wrapped
            in an OrderRequest object.
            """)
    public OrderResponse createOrder(@ToolParam(description = "The order request details (ISBN and Quantity)") OrderRequest orderRequest) {
        return orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }
}
