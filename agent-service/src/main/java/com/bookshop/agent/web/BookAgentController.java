package com.bookshop.agent.web;

import com.bookshop.agent.domain.BookAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/book")
@Slf4j
public class BookAgentController {

    private final BookAgentService bookAgentService;

    public BookAgentController(BookAgentService bookAgentService) {
        this.bookAgentService = bookAgentService;
    }

    @PostMapping("/search")
    public AgentBookResponse searchBook(@RequestHeader(name = "X_AI_CHAT_ID", defaultValue = "default") String chatId,
                                        @RequestBody SearchBookRequest request) {
        log.info(" Search Book Request [{}] with X_AI_CHAT_ID header = {}", request, chatId);
        return bookAgentService.searchBook(request, chatId);
    }
}
