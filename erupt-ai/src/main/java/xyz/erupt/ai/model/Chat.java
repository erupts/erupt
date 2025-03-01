package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.constants.ChatType;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.View;
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
@Erupt(
        name = "会话管理",
        power = @Power(add = false, edit = false, viewDetails = false), orderBy = "createdTime desc",
        drills = @Drill(title = "会话记录", link = @Link(linkErupt = ChatMessage.class, joinColumn = "chatId"))
)
@Entity
@EruptI18n
public class Chat extends BaseModel {

    @EruptField(
            views = @View(title = "会话类型")
    )
    @Enumerated(EnumType.STRING)
    private ChatType type;

    private Long typeRef;

    private String typeName;

    private Long userId;

    private String title;

    private String llmCode;

    @EruptField(
            views = @View(title = "创建时间")
    )
    private LocalDateTime createdTime;

}
