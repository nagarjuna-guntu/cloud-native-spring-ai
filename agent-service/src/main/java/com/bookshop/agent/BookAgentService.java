package com.bookshop.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class BookAgentService {

    private static final String SEARCH_BOOK_PROMPT = """
            Please search for the following book with search keyword:
            {keyword},
            
            The keyword can be a book title or part title, author, or any relevant keyword..

            populate AgentSearchResponse as follows:
            response:
            A friendly sentence describing the search result.
            books:
            Copy every returned book into the books list.
            Do not invent books.
            Do not invent ISBNs.
            If the tool returns no books,
            return
            response:
            "No books found."
            books:
            []
            """;

    private static final String PLACE_ORDER_PROMPT = """
            Order the following book.
            ISBN: {isbn}
            Quantity: {quantity}
           
            """;

    private final ChatClient chatClient;

    public BookAgentService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public AgentOrderResponse placeOrder(AgentOrderRequest agentRequest, String chatId) {

        var validationAdvisor = StructuredOutputValidationAdvisor
                .builder()
                .maxRepeatAttempts(2)
                .outputType(AgentOrderResponse.class)
                .build();

        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(PLACE_ORDER_PROMPT)
                        .param("isbn", agentRequest.isbn())
                        .param("quantity", agentRequest.quantity())
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .advisors(validationAdvisor)
                .call()
                .entity(AgentOrderResponse.class, entityParamSpec -> entityParamSpec
                        .useProviderStructuredOutput()
                        .validateSchema());
    }

    public AgentSearchResponse searchBook(AgentSearchRequest request, String chatId) {
        var validationAdvisor = StructuredOutputValidationAdvisor
                .builder()
                .maxRepeatAttempts(2)
                .outputType(AgentSearchResponse.class)
                .build();

        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(SEARCH_BOOK_PROMPT)
                        .param("keyword", request.keyword())
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .advisors(validationAdvisor)
                .call()
                .entity(AgentSearchResponse.class, entityParamSpec -> entityParamSpec
                        .useProviderStructuredOutput()
                        .validateSchema());

    }
}
