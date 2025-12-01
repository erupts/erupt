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
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.notice.constant.NoticeStatus;
import xyz.erupt.notice.modal.dataproxy.ChannelChoice;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;
import xyz.erupt.upms.model.EruptUserVo;

@EruptI18n
@Erupt(
        orderBy = "createTime desc,receiveUser.id",
        name = "Notice Log Details",
        power = @Power(export = true, add = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "e_notice_log_detail")
@Getter
@Setter
public class NoticeLogDetail extends HyperModelCreatorOnlyVo {

    @ManyToOne
    private NoticeLog noticeLog;

    @EruptField(
            views = @View(title = "notice.channel"),
            edit = @Edit(title = "notice.channel", type = EditType.CHOICE, search = @Search,
                    choiceType = @ChoiceType(fetchHandler = ChannelChoice.class))
    )
    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    @EruptField(
            views = @View(title = "notice.status"),
            edit = @Edit(title = "notice.status", type = EditType.CHOICE, notNull = true, search = @Search,
                    choiceType = @ChoiceType(fetchHandler = NoticeStatus.H.class))
    )
    private NoticeStatus status;

    @ManyToOne
    @EruptField(
            views = @View(title = "notice.receive_user", column = "name"),
            edit = @Edit(title = "notice.receive_user", type = EditType.REFERENCE_TABLE, search = @Search)
    )
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private EruptUserVo receiveUser;

    @EruptField(
            views = @View(title = "notice.is_success", sortable = true),
            edit = @Edit(title = "notice.is_success", search = @Search)
    )
    private Boolean success;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "notice.error", type = ViewType.HTML)
    )
    private String error;

}
