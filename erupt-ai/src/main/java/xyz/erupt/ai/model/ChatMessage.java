package xyz.erupt.ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.core.util.EruptTableStyle;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.linq.lambda.LambdaSee;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/2/27 22:57
 */
@Erupt(name = "会话管理", dataProxy = ChatMessage.class)
@Getter
@Setter
@Table(name = "e_ai_chat_message")
@Entity
@EruptI18n
@NoArgsConstructor
public class ChatMessage extends BaseModel implements DataProxy<ChatMessage> {

    private Long chatId;

    private String llm;

    private String model;

    @EruptField(
            views = @View(title = "发送人")
    )
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(100)")
    private ChatSenderType senderType;

    @EruptField(
            views = @View(title = "发送内容", type = ViewType.HTML)
    )
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String content;

    @EruptField(
            views = @View(title = "发送时间", type = ViewType.HTML)
    )
    private LocalDateTime createdAt;

    private Long tokens;

    public static ChatMessage create(Long chatId, String llm, String model, ChatSenderType senderType, String content, Long tokens) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatId(chatId);
        chatMessage.setLlm(llm);
        chatMessage.setModel(model);
        chatMessage.setSenderType(senderType);
        chatMessage.setContent(content);
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setTokens(tokens);
        return chatMessage;
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String senderType = LambdaSee.field(ChatMessage::getSenderType);
        for (Map<String, Object> map : list) {
            if (map.get(senderType) == ChatSenderType.MODEL) {
                EruptTableStyle.cellColor(map, senderType, "#09f");
            }
        }
    }
}
