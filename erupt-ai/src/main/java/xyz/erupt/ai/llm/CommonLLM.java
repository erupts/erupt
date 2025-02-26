package xyz.erupt.ai.llm;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.BaseLLMConfig;
import xyz.erupt.ai.base.SseListener;
import xyz.erupt.ai.base.SuperLLM;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.constants.MessageRole;
import xyz.erupt.ai.pojo.ChatCompletion;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
import xyz.erupt.ai.pojo.ChatCompletionResponse;
import xyz.erupt.ai.pojo.ChatCompletionStreamResponse;
import xyz.erupt.core.config.GsonFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2025/2/25 22:07
 */
@Component
public abstract class CommonLLM extends SuperLLM<BaseLLMConfig> {

    @Resource
    private AiProp aiProp;

    public String chatApiPath() {
        return "/v1/chat/completions";
    }

    @Override
    public ChatCompletionResponse chat(BaseLLMConfig config, String userPrompt, String assistantPrompt) {
        ChatCompletion completion = ChatCompletion.builder().model(config.getModel())
                .stream(false).messages(Arrays.asList(
                        new ChatCompletionMessage(MessageRole.system, aiProp.getSystemPrompt()),
                        new ChatCompletionMessage(MessageRole.user, userPrompt),
                        new ChatCompletionMessage(MessageRole.assistant, assistantPrompt)
                )).build();
        HttpResponse response = HttpUtil.createPost(config.getUrl() + chatApiPath())
                .header("Authorization", "Bearer " + config.getApiKey())
                .body(GsonFactory.getGson().toJson(completion))
                .execute();
        return GsonFactory.getGson().fromJson(response.body(), ChatCompletionResponse.class);
    }

    @Override
    @SneakyThrows
    public void chatSse(BaseLLMConfig config, String userPrompt, String assistantPrompt, Consumer<SseListener> listener) {
        ChatCompletion completion = ChatCompletion.builder().model(config.getModel()).
                messages(Arrays.asList(
                        new ChatCompletionMessage(MessageRole.system, aiProp.getSystemPrompt()),
                        new ChatCompletionMessage(MessageRole.user, userPrompt),
                        new ChatCompletionMessage(MessageRole.assistant, assistantPrompt)
                )).stream(true).build();
        HttpResponse response = HttpUtil.createPost(config.getUrl() + chatApiPath())
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer " + config.getApiKey())
                .body(GsonFactory.getGson().toJson(completion))
                .execute();

        if (response.isOk()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.bodyStream()))) {
                String line;
                SseListener sseListener = new SseListener();
                while ((line = reader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        if (line.startsWith("data: ")) {
                            line = line.substring(6);
                        }
                        if ("[DONE]".equalsIgnoreCase(line)) {
                            sseListener.setFinish(true);
                            listener.accept(sseListener);
                        } else {
                            ChatCompletionStreamResponse chatCompletionStreamResponse = GsonFactory.getGson().fromJson(line, ChatCompletionStreamResponse.class);
                            sseListener.setCurrData(line);
                            StringBuilder sb = new StringBuilder();
                            for (ChatCompletionStreamResponse.Choice choice : chatCompletionStreamResponse.getChoices()) {
                                if (null != choice.getUsage()) {
                                    sseListener.setUsage(sseListener.getUsage().plus(choice.getUsage()));
                                }
                                if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                    sseListener.getOutput().append(choice.getDelta().getContent());
                                    sb.append(choice.getDelta().getContent());
                                }
                            }
                            sseListener.setCurrMessage(sb.toString());
                            listener.accept(sseListener);
                        }
                    }
                }
            }
        } else {
            new Exception("Failed to get response from server" + response.body());
        }
    }
}
