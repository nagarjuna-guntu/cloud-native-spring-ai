package com.bookshop.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
@Slf4j
public class BookAgentController {

    private final BookAgentService bookAgentService;

    public BookAgentController(BookAgentService bookAgentService) {
        this.bookAgentService = bookAgentService;
    }

    @PostMapping("/search")
    public AgentSearchResponse searchBook(@RequestHeader(name = "X_AI_CHAT_ID", defaultValue = "default") String chatId,
                                          @RequestBody AgentSearchRequest request) {
        log.info(" Search Book X_AI_CHAT_ID = {}", chatId);
        return bookAgentService.searchBook(request, chatId);
    }

    @PostMapping("/order")
    public AgentOrderResponse placeOrder(@RequestHeader(name = "X_AI_CHAT_ID", defaultValue = "default") String chatId,
                                         @RequestBody AgentOrderRequest request) {
        log.info("Place Order X_AI_CHAT_ID = {}", chatId);
        return bookAgentService.placeOrder(request, chatId);
    }
}
