package xyz.erupt.webhook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.helper.HyperModelUpdateVo;
import xyz.erupt.upms.model.converter.StringSetJsonConverter;
import xyz.erupt.webhook.handler.EruptWebhookDataProxy;
import xyz.erupt.webhook.handler.WebhookEruptChoice;
import xyz.erupt.webhook.model.converter.WebhookEventSetConverter;

import java.util.EnumSet;
import java.util.Set;

/**
 * A subscription: which models and which events, delivered where, signed with what. The form has
 * a test button that posts the values as typed; the table shows the subscription as tags and
 * drills into the delivery log. The secret is
 * never shown back; the HMAC-SHA256 of each payload travels in the {@code X-Erupt-Signature} header
 * so the receiver can verify the sender.
 */
@EruptI18n
@Erupt(
        name = "Webhook",
        orderBy = "createTime desc",
        dataProxy = EruptWebhookDataProxy.class,
        power = @Power(revision = false, comment = false),
        drills = @Drill(title = "Deliveries", icon = "fa fa-list-ul",
                link = @Link(linkErupt = EruptWebhookLog.class, joinColumn = "webhookId")),
        rowOperation = @RowOperation(code = "ping", title = "Send Test", icon = "fa fa-paper-plane",
                mode = RowOperation.Mode.SINGLE, operationHandler = EruptWebhookDataProxy.class)
)
@Entity
@Table(name = "e_webhook")
@Getter
@Setter
public class EruptWebhook extends HyperModelUpdateVo {

    @Column(length = 100)
    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @Column(length = 1024)
    @EruptField(
            views = @View(title = "URL"),
            edit = @Edit(title = "URL", notNull = true, placeHolder = "https://", search = @Search(operator = QueryExpression.LIKE))
    )
    private String url;

    @Column(length = 255)
    @EruptField(
            edit = @Edit(title = "Secret", type = EditType.PASSWORD, desc = "Signs each payload as X-Erupt-Signature (HMAC-SHA256); empty sends unsigned")
    )
    private String secret;

    // a Set of an enum is a MULTI_CHOICE whose options are the constants, all of them to begin with;
    // both sets live as JSON arrays in their own column rather than in side tables
    @Convert(converter = WebhookEventSetConverter.class)
    @Column(length = 100)
    @EruptField(
            edit = @Edit(title = "Events", notNull = true, type = EditType.MULTI_CHOICE
                    , multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.SELECT)
            )
    )
    private Set<WebhookEvent> events = EnumSet.allOf(WebhookEvent.class);


    @EruptField(
            views = @View(title = "Enabled", sortable = true, width = "90px"),
            edit = @Edit(title = "Enabled", notNull = true, boolType = @BoolType(type = BoolType.Type.SWITCH), search = @Search)
    )
    private Boolean enabled = true;

    // extra request headers as a JSON object, e.g. an Authorization header the receiver expects
    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "Headers", type = EditType.KEY_VALUE, desc = "Extra request headers; e.g. Authorization")
    )
    private String headers;

    // posts a PING with the values as typed, so the endpoint is checked before the row is saved
    @Transient
    @EruptField(
            edit = @Edit(title = "Send Test", type = EditType.BUTTON,
                    buttonType = @ButtonType(icon = "fa fa-paper-plane", handler = EruptWebhookDataProxy.class))
    )
    private String sendTest;

    // on: every registered model fires; off: only the models picked below, which then become required
    @EruptField(
            edit = @Edit(title = "All Models", notNull = true, boolType = @BoolType(type = BoolType.Type.SWITCH),
                    desc = "Subscribe every registered model; switch off to pick models")
    )
    private Boolean allModels = false;

    // erupt names, as a JSON array; the required check lives in the data proxy, the frontend only hides the picker
    @Convert(converter = StringSetJsonConverter.class)
    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "Models", type = EditType.MULTI_CHOICE,
                    dynamic = @Dynamic(dependField = "allModels", condition = "!value", match = Dynamic.Ctrl.NOTNULL),
                    multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.TRANSFER, fetchHandler = WebhookEruptChoice.class))
    )
    private Set<String> erupts;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

    // The list query leaves collections out, so the subscription is summarised into these two
    // tag columns by the data proxy after each fetch; they never reach the form.
    @Transient
    @EruptField(
            views = @View(title = "Models"),
            edit = @Edit(title = "Models", show = false, type = EditType.TAGS)
    )
    private String modelTags;

    @Transient
    @EruptField(
            views = @View(title = "Events", width = "200px"),
            edit = @Edit(title = "Events", show = false, type = EditType.TAGS)
    )
    private String eventTags;

}
