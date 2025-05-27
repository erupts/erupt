package xyz.erupt.ai.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.constants.MessageRole;
import xyz.erupt.ai.pojo.ChatCompletion;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
import xyz.erupt.ai.pojo.ChatCompletionResponse;
import xyz.erupt.ai.pojo.ChatCompletionStreamResponse;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2025/2/25 22:07
 */
@Component
@Slf4j
public abstract class OpenAi extends LlmCore {

    private final Gson gson = new GsonBuilder().create();

    public String chatApiPath() {
        return "/v1/chat/completions";
    }

    @Override
    public ChatCompletionResponse chat(LlmRequest llmRequest, String userPrompt, List<ChatCompletionMessage> assistantPrompt) {
        assistantPrompt.add(new ChatCompletionMessage(MessageRole.user, userPrompt));
        ChatCompletion completion = ChatCompletion.builder().model(llmRequest.getModel()).stream(false).messages(assistantPrompt).build();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                gson.toJson(completion),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(llmRequest.getUrl() + chatApiPath())
                .post(body)
                .addHeader("Authorization", "Bearer " + llmRequest.getApiKey())
                .build();

        // 同步执行请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get response from server: " + response.body().string());
            }

            // 解析响应体为 ChatCompletionResponse
            return GsonFactory.getGson().fromJson(response.body().string(), ChatCompletionResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute HTTP request", e);
        }
    }

    @Override
    @SneakyThrows
    public void chatSse(LlmRequest llmRequest, String userPrompt, List<ChatCompletionMessage> assistantPrompt, Consumer<SseListener> listener) {
        ChatCompletion completion = ChatCompletion.builder().model(llmRequest.getModel()).messages(assistantPrompt).stream(true).build();
        completion.setResponse_format(new HashMap<String, String>() {{
            this.put("type", String.valueOf(llmRequest.getResponseFormat()));
        }});
        completion.setTopP(llmRequest.getTop_p());
        completion.setTemperature(llmRequest.getTemperature());
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                gson.toJson(completion),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(llmRequest.getUrl() + chatApiPath())
                .post(body)
                .addHeader("Accept", "text/event-stream")
                .addHeader("Authorization", "Bearer " + llmRequest.getApiKey())
                .build();
        MetaContext metaContext = MetaContext.get();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("Failed to get response from server", e);
                SseListener sseListener = new SseListener();
                sseListener.setError(true);
                sseListener.setFinish(true);
                sseListener.getOutput().append(e.getMessage());
                sseListener.setCurrMessage(e.getMessage());
                listener.accept(sseListener);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                MetaContext.set(metaContext);
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        BufferedSource source = responseBody.source();
                        SseListener sseListener = new SseListener();
                        while (!source.exhausted()) {
                            String line = source.readUtf8Line();
                            if (StringUtils.isNotBlank(line)) {
                                if (!response.isSuccessful()) {
                                    this.onFailure(call, new IOException(line));
                                } else {
                                    try {
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
                                    } catch (Exception e) {
                                        this.onFailure(call, new IOException(e));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

}
