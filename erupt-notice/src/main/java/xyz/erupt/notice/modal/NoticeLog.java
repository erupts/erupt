package xyz.erupt.notice.modal;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.notice.modal.dataproxy.ChannelChoice;
import xyz.erupt.notice.modal.dataproxy.EruptUserChoice;
import xyz.erupt.notice.modal.dataproxy.NoticeLogDataProxy;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

import java.util.Date;
import java.util.Set;

@EruptI18n
@Erupt(
        orderBy = "createTime desc",
        dataProxy = NoticeLogDataProxy.class,
        name = "Notice Log",
        linkTree = @LinkTree(field = "noticeScene"),
        drills = @Drill(title = "Notice Log Details", link = @Link(linkErupt = NoticeLogDetail.class, joinColumn = "noticeLog.id")),
        power = @Power(export = true, edit = false, viewDetails = false)
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

    @ManyToOne
    @EruptField(
            views = @View(title = "notice.scene", column = "name"),
            edit = @Edit(title = "notice.scene", notNull = true, type = EditType.REFERENCE_TABLE)
    )
    private NoticeScene noticeScene;

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


    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "URL", type = ViewType.LINK),
            edit = @Edit(title = "URL")
    )
    private String url;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "content", type = ViewType.HTML),
            edit = @Edit(title = "content", type = EditType.CODE_EDITOR, search = @Search(vague = true),
                    notNull = true, codeEditType = @CodeEditorType(language = "text"))
    )
    private String content;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Params", type = ViewType.CODE)
    )
    private String params;

    @EruptField(
            views = @View(title = "创建时间", sortable = true),
            edit = @Edit(title = "创建时间",search = @Search(vague = true), readonly = @Readonly(allowChange = false), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date createTime;

}
