package xyz.erupt.notice.modal;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.notice.modal.dataproxy.ChannelChoice;
import xyz.erupt.notice.modal.dataproxy.NoticeLogDataProxy;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;
import xyz.erupt.upms.model.EruptUserVo;

import java.time.LocalDateTime;
import java.util.Set;

@EruptI18n
@Erupt(
        orderBy = "createdAt desc",
        name = "Notice Log",
        drills = @Drill(title = "Notice Log", link = @Link(linkErupt = NoticeLogChannel.class, joinColumn = "noticeLog.id")),
        power = @Power(export = true, edit = false, viewDetails = false),
        dataProxy = NoticeLogDataProxy.class
)
@Entity
@Table(name = "e_notice_log")
@Getter
@Setter
public class NoticeLog extends HyperModelCreatorOnlyVo {

    @EruptField(
            views = @View(title = "标题"),
            edit = @Edit(title = "标题", notNull = true)
    )
    private String title;

    @ManyToOne
    @EruptField(
            views = @View(title = "接收人"),
            edit = @Edit(title = "接收人", type = EditType.REFERENCE_TABLE)
    )
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private EruptUserVo receiveUser;

    @Transient
    @EruptField(
            edit = @Edit(title = "通知渠道", type = EditType.MULTI_CHOICE, notNull = true,
                    multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.SELECT, fetchHandler = ChannelChoice.class))
    )
    private Set<String> channels;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "内容", type = ViewType.HTML),
            edit = @Edit(title = "内容", type = EditType.CODE_EDITOR, notNull = true, codeEditType = @CodeEditorType(language = "text"))
    )
    private String content;

}
