package xyz.erupt.ai.ask;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.TokenStream;

public interface EruptAiChat {

    AiMessage chat(String userMessage);

    TokenStream streamChat(String userMessage);

}
