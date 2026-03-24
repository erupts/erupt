package xyz.erupt.ai.ask;

import dev.langchain4j.service.TokenStream;

public interface EruptAiChat {

    TokenStream chat(String userMessage);
}
