package xyz.erupt.notice.modal;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
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
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.notice.modal.dataproxy.ChannelChoice;
import xyz.erupt.notice.modal.dataproxy.EruptUserChoice;
import xyz.erupt.notice.modal.dataproxy.NoticeLogDataProxy;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

import java.util.Set;

@EruptI18n
@Erupt(
        orderBy = "createTime desc",
        name = "Notice Log",
        drills = @Drill(title = "Log Details", link = @Link(linkErupt = NoticeLogDetail.class, joinColumn = "noticeLog.id")),
        power = @Power(export = true, edit = false, viewDetails = false),
        dataProxy = NoticeLogDataProxy.class
)
@EruptDataProcessor(value = "NoticeLog")
@Entity
@Table(name = "e_notice_log")
@Getter
@Setter
public class NoticeLog extends HyperModelCreatorOnlyVo {

    @EruptField(
            views = @View(title = "title"),
            edit = @Edit(title = "title", notNull = true, search = @Search(vague = true))
    )
    private String title;

    @Transient
    @EruptField(
            edit = @Edit(title = "notice.receive_user", type = EditType.MULTI_CHOICE, notNull = true,
                    multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.SELECT, fetchHandler = EruptUserChoice.class))
    )
    private Set<Long> receiveUsers;

    @Transient
    @EruptField(
            edit = @Edit(title = "notice.channel", type = EditType.MULTI_CHOICE, notNull = true,
                    multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.SELECT, fetchHandler = ChannelChoice.class))
    )
    private Set<String> channels;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "content", type = ViewType.HTML),
            edit = @Edit(title = "content", type = EditType.CODE_EDITOR, search = @Search(vague = true),
                    notNull = true, codeEditType = @CodeEditorType(language = "text"))
    )
    private String content;

}
