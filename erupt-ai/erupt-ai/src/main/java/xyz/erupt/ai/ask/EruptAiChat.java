package xyz.erupt.ai.ask;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface EruptAiChat {

    // The explicit {{it}} template keeps AiServices from parsing the message
    // content itself as a prompt template (user content may contain {{...}},
    // e.g. Vue mustache syntax in AI-generated pages)
    @UserMessage("{{it}}")
    AiMessage chat(@V("it") String userMessage);

    @UserMessage("{{it}}")
    TokenStream streamChat(@V("it") String userMessage);

    // Multimodal variant (text + images): a List<Content> argument makes AiServices
    // build the user message from the contents directly, no template parsing involved
    TokenStream streamChat(List<Content> contents);

}
