package xyz.erupt.config.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.config.boot.ConfigValues;
import xyz.erupt.config.form.ConfigCopyForm;
import xyz.erupt.config.handler.ConfigItemHandler;
import xyz.erupt.config.proxy.ConfigItemDataProxy;
import xyz.erupt.jpa.model.MetaModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * One key of one profile. The page lists the profiles on the left and the picked profile's keys
 * on the right. Saving an item refreshes the running values of this instance; other instances
 * pick it up within {@code erupt.config.refresh-seconds}.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = ConfigItem.TABLE, uniqueConstraints = @UniqueConstraint(columnNames = {ConfigItem.PROFILE_COLUMN, ConfigItem.KEY_COLUMN}))
@Erupt(name = "Config items", dataProxy = ConfigItemDataProxy.class, orderBy = "key", power = @Power(export = false),
        linkTree = @LinkTree(field = "profile", dependNode = true),
        rowOperation = @RowOperation(code = "copy", title = "Copy to profile", icon = "fa fa-copy", mode = RowOperation.Mode.MULTI,
                tip = "Copies the selected keys into another profile, replacing values already there",
                eruptClass = ConfigCopyForm.class, operationHandler = ConfigItemHandler.class))
public class ConfigItem extends MetaModel {

    public static final String TABLE = "e_config_item";

    public static final String PROFILE_COLUMN = "profile_id";

    public static final String KEY_COLUMN = "config_key";

    public static final String VALUE_COLUMN = "config_value";

    public static final String TYPE_COLUMN = "config_type";

    public static final String ENABLED_COLUMN = "enabled";

    public static final String MASK = "••••••";

    // The profile picked in the tree: set by the link header on add, kept on edit, never typed in the form.
    // The tree on the left is drawn from referenceTreeType: the profiles are a flat list labelled by code
    @ManyToOne
    @JoinColumn(name = PROFILE_COLUMN)
    @EruptField(edit = @Edit(title = "Profile", show = false, readonly = @Readonly(allowChange = false), type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(label = "code"), referenceTreeType = @ReferenceTreeType(label = "code")))
    private ConfigProfile profile;

    @Column(name = KEY_COLUMN)
    @EruptField(views = @View(title = "Key"), edit = @Edit(title = "Key", notNull = true, search = @Search(operator = QueryExpression.LIKE),
            inputType = @InputType(regex = "^[A-Za-z][A-Za-z0-9._\\-\\[\\]]*$"), desc = "As read by @Value: app.mail.host, feature.beta"))
    private String key;

    @Column(name = TYPE_COLUMN)
    @EruptField(views = @View(title = "Type"), edit = @Edit(title = "Type", notNull = true, search = @Search, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = ConfigValues.STRING, label = "String"), @VL(value = ConfigValues.PASSWORD, label = "Password"),
            @VL(value = ConfigValues.TEXT, label = "Text"), @VL(value = ConfigValues.NUMBER, label = "Number"),
            @VL(value = ConfigValues.BOOLEAN, label = "Boolean"), @VL(value = ConfigValues.LIST, label = "List"),
            @VL(value = ConfigValues.KEY_VALUE, label = "Key / value"), @VL(value = ConfigValues.JSON, label = "JSON"),
            @VL(value = ConfigValues.YAML, label = "YAML"), @VL(value = ConfigValues.DATE, label = "Date"),
            @VL(value = ConfigValues.DATETIME, label = "Datetime"), @VL(value = ConfigValues.TIME, label = "Time"),
            @VL(value = ConfigValues.COLOR, label = "Color"), @VL(value = ConfigValues.ICON, label = "Icon"),
            @VL(value = ConfigValues.HTML, label = "HTML"), @VL(value = ConfigValues.IMAGE, label = "Image"),
            @VL(value = ConfigValues.ATTACHMENT, label = "Attachment")
    })))
    private String type = ConfigValues.STRING;

    // What is stored and read, whatever the type; the editors below write it on save
    @Column(name = VALUE_COLUMN, length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(views = @View(title = "Value"), edit = @Edit(title = "Value", cellEdit = false,
            dynamic = @Dynamic(dependField = "type", condition = "value === 'STRING'")))
    private String value;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.PASSWORD, desc = "Always a secret: masked in the table and the form",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'PASSWORD'")))
    private String passwordValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.TEXTAREA, textareaType = @TextareaType(length = 4000, minRows = 3, maxRows = 12),
            dynamic = @Dynamic(dependField = "type", condition = "value === 'TEXT'")))
    private String textValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.NUMBER, dynamic = @Dynamic(dependField = "type", condition = "value === 'NUMBER'")))
    private Double numberValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.BOOLEAN, boolType = @BoolType(trueText = "true", falseText = "false"),
            dynamic = @Dynamic(dependField = "type", condition = "value === 'BOOLEAN'")))
    private Boolean boolValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.TAGS, tagsType = @TagsType(joinSeparator = ","), desc = "Stored comma-separated; binds to a List or an array",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'LIST'")))
    private String listValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.KEY_VALUE, desc = "Each entry becomes key.entry, so a Map property binds to it",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'KEY_VALUE'")))
    private Map<String, String> mapValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"), desc = "Flattened into key.path and key[i] properties, the raw text kept under key",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'JSON'")))
    private String jsonValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "yaml"), desc = "A yml fragment; flattened into key.path properties the way Spring reads a file",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'YAML'")))
    private String yamlValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.DATE, dateType = @DateType(type = DateType.Type.DATE),
            dynamic = @Dynamic(dependField = "type", condition = "value === 'DATE'")))
    private LocalDate dateValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.DATE, dateType = @DateType(type = DateType.Type.DATE_TIME),
            dynamic = @Dynamic(dependField = "type", condition = "value === 'DATETIME'")))
    private LocalDateTime dateTimeValue;

    // A String on purpose: erupt's Gson has no LocalTime adapter, and the picker already hands over HH:mm:ss
    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.DATE, dateType = @DateType(type = DateType.Type.TIME),
            dynamic = @Dynamic(dependField = "type", condition = "value === 'TIME'")))
    private String timeValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.COLOR, colorType = @ColorType,
            dynamic = @Dynamic(dependField = "type", condition = "value === 'COLOR'")))
    private String colorValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.ICON, desc = "A Font Awesome class list, read by the application as a string",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'ICON'")))
    private String iconValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.HTML_EDITOR, htmlEditorType = @HtmlEditorType(HtmlEditorType.Type.CKEDITOR),
            dynamic = @Dynamic(dependField = "type", condition = "value === 'HTML'")))
    private String htmlValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.ATTACHMENT, attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE),
            desc = "The property is the picture's path under erupt.upload-path",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'IMAGE'")))
    private String imageValue;

    @Transient
    @EruptField(edit = @Edit(title = "Value", type = EditType.ATTACHMENT, attachmentType = @AttachmentType, desc = "The property is the file's path under erupt.upload-path",
            dynamic = @Dynamic(dependField = "type", condition = "value === 'ATTACHMENT'")))
    private String fileValue;

    @Column(name = ENABLED_COLUMN)
    @EruptField(views = @View(title = "Enabled"), edit = @Edit(title = "Enabled", notNull = true, search = @Search,
            boolType = @BoolType(trueText = "On", falseText = "Off"), desc = "Off keeps the item but the application no longer reads it"))
    private Boolean enabled = true;

    @EruptField(views = @View(title = "Secret"), edit = @Edit(title = "Secret", notNull = true, search = @Search,
            boolType = @BoolType(trueText = "Masked", falseText = "Plain"), desc = "Shown masked in the table; still readable by the application"))
    private Boolean secret = false;

    @EruptField(views = @View(title = "Remark"), edit = @Edit(title = "Remark", type = EditType.TEXTAREA, search = @Search(operator = QueryExpression.LIKE)))
    private String remark;

}
