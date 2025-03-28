package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;

import javax.persistence.*;
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
@Where(clause = "deleted = false or deleted is null")
public class Chat extends BaseModel {

    @EruptField(
            views = @View(title = "会话标题"),
            edit = @Edit(title = "会话标题", search = @Search(vague = true))
    )
    private String title;

    @EruptField(
            views = @View(title = "用户", column = "name")
    )
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private EruptUserVo eruptUser;

    private Boolean deleted = false;

    @EruptField(
            views = @View(title = "创建时间")
    )
    private LocalDateTime createdTime;

}
