package com.bookshop.agent.config;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AgentMcpTools {

    private final ToolCallback searchBook;
    private final ToolCallback placeOrder;
    private final ToolCallback findOrder;

    public AgentMcpTools(SyncMcpToolCallbackProvider mcpTools) {

        Map<String, ToolCallback> toolMap =
                Arrays.stream(mcpTools.getToolCallbacks())
                        .collect(Collectors.toUnmodifiableMap(
                                toolCallback -> toolCallback.getToolDefinition().name(),
                                Function.identity()));

        this.searchBook = initializeTool(toolMap, "searchBook");
        this.placeOrder = initializeTool(toolMap, "placeOrder");
        this.findOrder = initializeTool(toolMap, "findOrder");

    }

    private ToolCallback initializeTool(Map<String, ToolCallback> toolMap, String toolName) {
        ToolCallback tool = toolMap.get(toolName);
        if (tool == null) {
            throw new IllegalStateException("Required MCP tool not found: " + toolName);
        }
        return tool;
    }

    public ToolCallback searchBook() {
        return searchBook;
    }

    public ToolCallback placeOrder() {
        return placeOrder;
    }

    public ToolCallback findOrder() {
        return findOrder;
    }
}
