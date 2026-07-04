package xyz.erupt.ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;

import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2025/2/27 22:57
 */
@Getter
@Setter
@Table(name = "e_ai_chat")
@Erupt(
        name = "Chat Management",
        power = @Power(add = false, edit = false, viewDetails = false), orderBy = "createdTime desc",
        drills = @Drill(title = "Chat Records", link = @Link(linkErupt = AiChatMessage.class, joinColumn = "chatId"))
)
@Entity
@EruptI18n
@Where(clause = "deleted = false or deleted is null")
public class AiChat extends BaseModel {

    @EruptField(
            views = @View(title = "Chat Title"),
            edit = @Edit(title = "Chat Title", search = @Search(operator = QueryExpression.LIKE))
    )
    private String title;

    @EruptField(
            views = @View(title = "User", column = "name")
    )
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private EruptUserVo eruptUser;

    private Boolean deleted = false;

    @EruptField(
            views = @View(title = "Created Time")
    )
    private LocalDateTime createdTime;

}
