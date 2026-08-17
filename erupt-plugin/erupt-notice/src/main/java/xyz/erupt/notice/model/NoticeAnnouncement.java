package xyz.erupt.notice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.notice.constant.AnnouncementStatus;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

@EruptI18n
@Erupt(
        orderBy = "createTime desc",
        name = "Announcement",
        power = @Power(export = true)
)
@Entity
@Table(name = "e_notice_announcement")
@Getter
@Setter
public class NoticeAnnouncement extends HyperModelCreatorOnlyVo {

    @EruptField(
            views = @View(title = "title"),
            edit = @Edit(title = "title", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String title;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "status"),
            edit = @Edit(title = "status", notNull = true, type = EditType.CHOICE, search = @Search,
                    choiceType = @ChoiceType(fetchHandler = AnnouncementStatus.H.class))
    )
    private AnnouncementStatus status = AnnouncementStatus.OPEN;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "content", type = ViewType.HTML),
            edit = @Edit(title = "content", type = EditType.HTML_EDITOR, notNull = true)
    )
    private String content;

}
