package xyz.erupt.notice.modal;


import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.notice.modal.dataproxy.ChannelChoice;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

@EruptI18n
@Erupt(
        orderBy = "createdAt desc",
        name = "Notice Log Channel",
        power = @Power(export = true, add = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "e_notice_log_channel")
@Getter
@Setter
public class NoticeLogChannel extends HyperModelCreatorOnlyVo {

    @ManyToOne
    private NoticeLog noticeLog;

    @EruptField(
            views = @View(title = "Channel"),
            edit = @Edit(title = "Channel", type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = ChannelChoice.class))
    )
    private String channel;

    @EruptField(
            views = @View(title = "错误信息", type = ViewType.HTML)
    )
    private String error;


}
