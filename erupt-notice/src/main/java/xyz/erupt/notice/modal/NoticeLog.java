package xyz.erupt.notice.modal;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.notice.notify.NoticeHandler;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

import java.time.LocalDateTime;
import java.util.List;

@EruptI18n
@Erupt(
        orderBy = "createdAt desc",
        name = "Notice Log",
        power = @Power(export = true, add = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "e_notice_log")
@Getter
@Setter
public class NoticeLog extends HyperModelCreatorOnlyVo implements ChoiceFetchHandler {

    @EruptField(
            views = @View(title = "标题"),
            edit = @Edit(title = "标题")
    )
    private String title;

    @EruptField(
            views = @View(title = "内容"),
            edit = @Edit(title = "内容")
    )
    private String content;

    @EruptField(
            views = @View(title = "Channel"),
            edit = @Edit(title = "Channel", type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = NoticeLog.class))
    )
    private String channel;

    @EruptField(
            views = @View(title = "发送时间"),
            edit = @Edit(title = "发送时间")
    )
    private LocalDateTime createdAt;

    @EruptField(
            views = @View(title = "接收人"),
            edit = @Edit(title = "接收人")
    )
    private Long receiveUserId;

    @Override
    public List<VLModel> fetch(String[] params) {
        return NoticeHandler.getHandlers().values().stream().map(noticeHandler
                -> new VLModel(noticeHandler.code(), noticeHandler.name())).toList();
    }

}
