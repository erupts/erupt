package xyz.erupt.designer.pojo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.List;
import java.util.Optional;

/**
 * Design produced by the visual form designer.
 * <p>
 * The {@code erupt} / {@code view} / {@code edit} json objects mirror the structure of the
 * {@link xyz.erupt.annotation.Erupt} / {@link xyz.erupt.annotation.sub_field.View} /
 * {@link xyz.erupt.annotation.sub_field.Edit} annotations (raw member names), so the backend
 * can convert them to annotation source code generically by reflection.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Getter
@Setter
public class DesignerForm {

    private String pkg;

    private String className;

    private String tableName;

    // @Erupt annotation structure
    private JsonObject erupt;

    private List<DesignerField> fields;

    @Getter
    @Setter
    public static class DesignerField {

        // Edit, View and their sub annotations all name the member "type"
        private static final String TYPE = LambdaSee.method(Edit::type);

        private static final String DATE_TYPE = LambdaSee.method(Edit::dateType);

        private static final String ATTACHMENT_TYPE = LambdaSee.method(Edit::attachmentType);

        // immutable field identity, assigned on first publish. It survives renames, so the
        // storage layer can move an existing column instead of leaving its data orphaned
        // under the old name. Absent on designs published before ids existed.
        private String id;

        private String fieldName;

        // explicit java type, e.g. Integer / Long / Double / BigDecimal
        private String fieldType;

        // linked @Erupt class name for reference / tab types
        private String linkErupt;

        // @View annotation structure (nullable → no table column).
        // Declared as JsonElement: gson maps an explicit json null to JsonNull,
        // which fails to deserialize into a JsonObject-typed field.
        private JsonElement view;

        // @Edit annotation structure
        private JsonObject edit;

        public JsonObject getView() {
            return null == view || view.isJsonNull() ? null : view.getAsJsonObject();
        }

        /**
         * Settle a concrete view type on the design, because AUTO never resolves for a designed
         * field: ViewProxy derives it from the edit annotation of the template class rather than
         * of the design, so a color picker, a rich text editor or a date time would all fall
         * through to plain TEXT in the table. Returns null for a field the design leaves out of
         * the table, and a publish persists whatever it settles.
         */
        public ViewType viewType() {
            JsonObject view = this.getView();
            if (null == view) return null;
            ViewType viewType = view.has(TYPE) ? ViewType.valueOf(view.get(TYPE).getAsString()) : ViewType.AUTO;
            if (ViewType.AUTO == viewType) {
                viewType = this.deriveViewType();
                view.addProperty(TYPE, viewType.name());
            }
            return viewType;
        }

        // mirrors ViewProxy's AUTO inference
        private ViewType deriveViewType() {
            return switch (this.editType()) {
                case ATTACHMENT -> AttachmentType.Type.IMAGE.name().equals(this.editSubType(ATTACHMENT_TYPE))
                        ? ViewType.IMAGE : ViewType.ATTACHMENT;
                case SIGNATURE -> ViewType.IMAGE_BASE64;
                case DATE -> DateType.Type.DATE_TIME.name().equals(this.editSubType(DATE_TYPE))
                        ? ViewType.DATE_TIME : ViewType.DATE;
                case HTML_EDITOR -> ViewType.HTML;
                case CODE_EDITOR -> ViewType.CODE;
                case MARKDOWN -> ViewType.MARKDOWN;
                case PASSWORD -> ViewType.PASSWORD;
                case MAP -> ViewType.MAP;
                case COLOR -> ViewType.COLOR;
                case BOOLEAN -> ViewType.BOOLEAN;
                case NUMBER, SLIDER, RATE -> ViewType.NUMBER;
                case MULTI_CHOICE, CHECKBOX, MULTI_FORM, TAB_TREE, TAB_TABLE_ADD, TAB_TABLE_REFER -> ViewType.TAB_VIEW;
                default -> ViewType.TEXT;
            };
        }

        private EditType editType() {
            return Optional.ofNullable(edit).filter(it -> it.has(TYPE))
                    .map(it -> EditType.valueOf(it.get(TYPE).getAsString())).orElse(EditType.INPUT);
        }

        // type of an edit sub annotation, e.g. dateType: {type: "DATE_TIME"}
        private String editSubType(String member) {
            if (null == edit || !edit.has(member) || !edit.get(member).isJsonObject()) return null;
            JsonObject sub = edit.getAsJsonObject(member);
            return sub.has(TYPE) ? sub.get(TYPE).getAsString() : null;
        }
    }

}
