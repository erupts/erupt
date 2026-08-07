package xyz.erupt.ai_staff.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.ai_staff.proxy.AiStaffChannelProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * An IM channel endpoint (DingTalk / WeCom / Feishu / Slack ...): pushes staff
 * work reports outbound and routes inbound bot messages to the answering staff.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Erupt(
        name = "Channel Integration",
        dataProxy = AiStaffChannelProxy.class,
        rowOperation = @RowOperation(code = "testPush", icon = "fa fa-paper-plane", title = "Test Push",
                mode = RowOperation.Mode.SINGLE, operationHandler = AiStaffChannelProxy.class)
)
@Table(name = "e_ai_staff_channel")
@Getter
@Setter
@Entity
@EruptI18n
public class AiStaffChannel extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Channel Type"),
            edit = @Edit(title = "Channel Type", notNull = true, type = EditType.CHOICE, search = @Search,
                    onchange = AiStaffChannelProxy.class,
                    choiceType = @ChoiceType(fetchHandler = StaffChannel.H.class))
    )
    private String type;

    @ManyToOne
    @JoinColumn(name = "staff_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Answering Staff", column = "name"),
            edit = @Edit(title = "Answering Staff", type = EditType.REFERENCE_TABLE,
                    desc = "Staff who answers messages from this channel; leave blank for push-only")
    )
    private AiStaff staff;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status", notNull = true, search = @Search,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    @EruptField(
            views = @View(title = "Callback Code", width = "120px"),
            edit = @Edit(title = "Callback Code", notNull = true, readonly = @Readonly(add = false),
                    desc = "Callback URL: {domain}/erupt-api/ai-staff/channel/{code}")
    )
    private String code;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Channel Config"),
            edit = @Edit(title = "Channel Config", notNull = true, type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "json"))
    )
    private String config;

}
