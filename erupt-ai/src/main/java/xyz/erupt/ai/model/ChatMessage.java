package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.jpa.model.MetaModelCreateOnly;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2025/2/27 22:57
 */
@Erupt(name = "会话管理")
@Getter
@Setter
@Table(name = "e_ai_chat_message")
@Entity
@EruptI18n
public class ChatMessage extends MetaModelCreateOnly {

    private Long chatId;

    @Enumerated(EnumType.STRING)
    private ChatSenderType senderType;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String content;

    private LocalDateTime createdTime;

    public static ChatMessage create(Long chatId, ChatSenderType senderType, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatId(chatId);
        chatMessage.setSenderType(senderType);
        chatMessage.setContent(content);
        chatMessage.setCreateTime(LocalDateTime.now());
        return chatMessage;
    }

}
