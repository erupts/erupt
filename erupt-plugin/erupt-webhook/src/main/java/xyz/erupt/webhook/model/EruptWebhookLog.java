package xyz.erupt.webhook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.webhook.handler.WebhookResendHandler;

import java.time.LocalDateTime;

/**
 * One row per delivery attempt sequence: what was sent where, how the receiver answered and how many
 * tries it took. Written by the delivery worker only; administrators can read, filter, purge and
 * resend it, which posts the stored payload again as a fresh delivery.
 */
@EruptI18n
@Erupt(
        name = "Webhook Log",
        orderBy = "createTime desc",
        power = @Power(add = false, edit = false, export = true, revision = false, comment = false),
        rowOperation = @RowOperation(code = "resend", title = "Resend", icon = "fa fa-rotate-right",
                operationHandler = WebhookResendHandler.class)
)
@Entity
@Table(name = "e_webhook_log", indexes = {@Index(columnList = "createTime"), @Index(columnList = "webhookId")})
@Getter
@Setter
public class EruptWebhookLog extends BaseModel {

    // the subscription this delivery belongs to; the drill from the webhook table joins on it
    private Long webhookId;

    @Column(length = 100)
    @EruptField(
            views = @View(title = "Webhook", width = "140px"),
            edit = @Edit(title = "Webhook", search = @Search(operator = QueryExpression.LIKE))
    )
    private String webhook;

    @Column(length = 100)
    @EruptField(
            views = @View(title = "Model", width = "140px"),
            edit = @Edit(title = "Model", search = @Search)
    )
    private String erupt;

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Event", width = "90px"),
            edit = @Edit(title = "Event", search = @Search)
    )
    private WebhookEvent event;

    @Column(length = 100)
    @EruptField(
            views = @View(title = "Record ID", width = "110px"),
            edit = @Edit(title = "Record ID", search = @Search)
    )
    private String recordId;

    @Column(length = 1024)
    @EruptField(
            views = @View(title = "URL"),
            edit = @Edit(title = "URL")
    )
    private String url;

    @EruptField(
            views = @View(title = "Result", width = "90px"),
            edit = @Edit(title = "Result", search = @Search, boolType = @BoolType(trueText = "Delivered", falseText = "Failed"))
    )
    private Boolean success;

    @EruptField(
            views = @View(title = "Status", width = "80px"),
            edit = @Edit(title = "Status", search = @Search)
    )
    private Integer status;

    @EruptField(
            views = @View(title = "Attempts", width = "80px"),
            edit = @Edit(title = "Attempts")
    )
    private Integer attempts;

    @EruptField(
            views = @View(title = "Duration (ms)", width = "100px"),
            edit = @Edit(title = "Duration (ms)")
    )
    private Long duration;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Response", show = false),
            edit = @Edit(title = "Response", type = EditType.TEXTAREA)
    )
    private String response;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Payload", show = false),
            edit = @Edit(title = "Payload", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String payload;

    @EruptField(
            views = @View(title = "Time", sortable = true, width = "160px"),
            edit = @Edit(title = "Time", search = @Search)
    )
    private LocalDateTime createTime;

}
