//package xyz.erupt.ai.llm;
//
//import cn.hutool.http.HttpResponse;
//import cn.hutool.http.HttpUtil;
//import org.springframework.stereotype.Component;
//import xyz.erupt.ai.base.BaseLLMConfig;
//import xyz.erupt.ai.base.SseListener;
//import xyz.erupt.ai.base.SuperLLM;
//import xyz.erupt.ai.model.LLM;
//import xyz.erupt.ai.pojo.ChatCompletionResponse;
//import xyz.erupt.core.config.GsonFactory;
//
//import java.util.HashMap;
//import java.util.function.Consumer;
//
///**
// * @author YuePeng
// * date 2025/2/22 16:37
// */
//@Component
//public class Ollama extends SuperLLM<BaseLLMConfig> {
//    @Override
//    public String code() {
//        return "Ollama";
//    }
//
//    @Override
//    public String model() {
//        return "llama2-7b";
//    }
//
//    @Override
//    public BaseLLMConfig config() {
//        return new BaseLLMConfig("http://localhost:11434", "");
//    }
//
//    @Override
//    public ChatCompletionResponse chat(LLM llm, String userPrompt, String assistantPrompt) {
//        return null;
//    }
//
//    @Override
//    public void chatSse(LLM llm, String userPrompt, String assistantPrompt, Consumer<SseListener> listener) {
////        HttpResponse response = HttpUtil.createPost(baseLLMConfig.getUrl() + "/api/chat")
////                .header("Accept", "text/event-stream")
////                .body(GsonFactory.getGson().toJson(new HashMap<String, Object>() {{
////                    this.put("model", baseLLMConfig.getModel());
////                    this.put("stream", true);
////                    this.put("temperature", 0.7);
//////                    this.put("prompt", userPrompt);
////                }}))
////                .execute();
//
//    }
//}
