package xyz.erupt.ai.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.base.SuperLLM;
import xyz.erupt.core.constant.EruptRestPath;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2025/2/22 16:35
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/chat")
public class ChatController {

    private final ExecutorService sseExecutorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 4,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

    @GetMapping("/send")
    public SseEmitter send(@RequestParam String llmCode, @RequestParam String message) {
        SuperLLM<Object> llm = (SuperLLM<Object>) SuperLLM.getLLM(llmCode);
        if (llm == null) throw new RuntimeException("llm not found");
        SseEmitter emitter = new SseEmitter();
        sseExecutorService.submit(() -> llm.chatSse(llm.config(), message, "回答不超过10个字", (it) -> {
            try {
                if (it.isFinish()) {
                    System.out.println(it.getOutput().toString());
                    emitter.complete();
                } else {
                    emitter.send(it.getCurrMessage(), MediaType.TEXT_PLAIN);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }));
        return emitter;
    }

    @GetMapping("/list")
    public void list() {

    }

    @GetMapping("/messages")
    public void messages(@RequestParam Long chatId, @RequestParam Integer size,
                         @RequestParam(defaultValue = "0") Integer index) {

    }

}
