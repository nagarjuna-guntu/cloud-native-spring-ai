package com.bookshop.agent.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class AgentPromptTemplates {
    private static final String BASE_PATH = "classpath:/prompts/";
    private final ResourceLoader resourceLoader;

    public AgentPromptTemplates(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Resource get(String promptName) {
        return resourceLoader.getResource(BASE_PATH + promptName + ".st");
    }
}
