package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.constants.ChatType;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2025/2/27 22:57
 */
@Getter
@Setter
@Table(name = "e_ai_chat")
@Erupt(name = "会话管理")
@Entity
@EruptI18n
public class Chat extends BaseModel {

    @Enumerated(EnumType.STRING)
    private ChatType type;

    private Long typeRef;

    private String typeName;

    private Long userId;

    private String title;

    private LocalDateTime createdTime;

}
