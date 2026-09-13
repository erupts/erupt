package xyz.erupt.ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2025/2/27 22:57
 */
@Getter
@Setter
@Table(name = "e_ai_chat_message")
@Entity
@NoArgsConstructor
public class AiChatMessage extends BaseModel {

    private Long chatId;

    private String llm;

    private String model;

    @Enumerated(EnumType.STRING)
    private ChatSenderType senderType;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String content;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String thinkingContent;

    // JSON array of tool calls: [{"name":"...","args":"...","result":"..."}]
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String toolCalls;

    // JSON array of image attachment paths sent with a user message: ["/2026-08-31/xxx.png"]
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String images;

    private LocalDateTime createdAt;

    private Long agentId;

    private Integer tokens;

    // True when the user stopped the generation before it finished
    private Boolean interrupted;

    public static AiChatMessage create(Long chatId, String llm, String model, ChatSenderType senderType, String content, Integer tokens) {
        return create(chatId, llm, model, senderType, content, null, null, tokens);
    }

    public static AiChatMessage create(Long chatId, String llm, String model, ChatSenderType senderType, String content, String thinkingContent, Integer tokens) {
        return create(chatId, llm, model, senderType, content, thinkingContent, null, tokens);
    }

    public static AiChatMessage create(Long chatId, String llm, String model, ChatSenderType senderType, String content, String thinkingContent, String toolCalls, Integer tokens) {
        AiChatMessage chatMessage = new AiChatMessage();
        chatMessage.setChatId(chatId);
        chatMessage.setLlm(llm);
        chatMessage.setModel(model);
        chatMessage.setSenderType(senderType);
        chatMessage.setContent(content);
        chatMessage.setThinkingContent(thinkingContent);
        chatMessage.setToolCalls(toolCalls);
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setTokens(tokens);
        return chatMessage;
    }

}
