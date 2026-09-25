package com.bookshop.agent.domain;


import com.bookshop.agent.config.AgentMcpTools;
import com.bookshop.agent.config.AgentPromptTemplates;
import com.bookshop.agent.web.AgentBookResponse;
import com.bookshop.agent.web.SearchBookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
public class BookAgentService {

    private final ChatClient chatClient;
    private final AgentMcpTools mcpTools;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;
    private final AgentPromptTemplates promptTemplates;
    private final JsonMapper jsonMapper;

    public BookAgentService(ChatClient chatClient,
                            AgentMcpTools mcpTools,
                            MessageChatMemoryAdvisor chatMemoryAdvisor,
                            AgentPromptTemplates promptTemplates,
                            JsonMapper jsonMapper) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
        this.chatMemoryAdvisor = chatMemoryAdvisor;
        this.promptTemplates = promptTemplates;
        this.jsonMapper = jsonMapper;
    }

    public AgentBookResponse searchBook(SearchBookRequest request, String chatId) {

        log.info("SearchBook agent workflow for search request [{}] : chatId: [{}] - START", request, chatId);

        AgentSearchResult agentSearchResult = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(promptTemplates.get("searchBook"))
                        .param("keyword", request.keyword())
                )
                .advisors(advisorSpec -> advisorSpec
                        .advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .tools(mcpTools.searchBook())
                .call()
                .entity(AgentSearchResult.class, ChatClient.EntityParamSpec::validateSchema);

        String message = agentSearchResult.books().isEmpty()
                ? "No Book(s) Found."
                : " %d Book(s) Found.".formatted(agentSearchResult.books().size());

        log.info("SearchBook agent workflow for search request [{}] : chatId: [{}] - RETURNED", request, chatId);

        return new AgentBookResponse(message, agentSearchResult.books());
    }
}
