package xyz.erupt.decision.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.annotation.sub_field.sub_edit.KeyValueType;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.decision.constant.DecisionConst;
import xyz.erupt.decision.constant.PrimitiveType;
import xyz.erupt.decision.question.Question;
import xyz.erupt.jpa.model.BaseModel;

/**
 * One question of a stored {@link DecisionDef}. Its code is the key the answer comes back
 * under, so it is the name callers write against.
 * <p>
 * The rubric is edited through the component that fits the question type — a key-value list for
 * a choice, an ordered tag list for a score, two lines of prose for a yes/no — and is stored as
 * the one json column the provider is sent.
 *
 * @author YuePeng
 */
@Entity
@Table(name = "e_ai_decision_question")
@Erupt(name = "Decision Question", orderBy = "sort")
@Getter
@Setter
@EruptI18n
public class DecisionQuestion extends BaseModel {

    private static final String YES = "true";

    private static final String NO = "false";

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Question Code", sortable = true),
            edit = @Edit(title = "Question Code", notNull = true, desc = "The key this question's answer is returned under")
    )
    private String code;

    @EruptField(
            views = @View(title = "Sort", sortable = true),
            edit = @Edit(title = "Sort")
    )
    private Integer sort;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Type"),
            edit = @Edit(title = "Type", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(type = ChoiceType.Type.RADIO, fetchHandler = PrimitiveType.H.class))
    )
    private PrimitiveType type = PrimitiveType.NOUL;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Instructions"),
            edit = @Edit(title = "Instructions", notNull = true, type = EditType.TEXTAREA,
                    desc = "What the model decides about the state. Plain text, or json to give it structure")
    )
    private String instructions;

    @Transient
    @EruptField(
            edit = @Edit(title = "Options", type = EditType.KEY_VALUE,
                    desc = "Each option and when it applies. The model reads the description, so write it as a rule",
                    keyValueType = @KeyValueType(keyPlaceholder = "Option", valuePlaceholder = "When this option applies",
                            max = DecisionConst.MAX_CHOICE_OPTIONS),
                    dynamic = @Dynamic(dependField = "type", condition = "value === 'CHOICE'",
                            match = Dynamic.Ctrl.NOTNULL))
    )
    private String options;

    @Transient
    @EruptField(
            edit = @Edit(title = "Levels", type = EditType.TAGS,
                    desc = "Ordered levels, lowest first — the answer is weighted across them",
                    tagsType = @TagsType(maxTagCount = DecisionConst.MAX_SCORE_LEVELS),
                    dynamic = @Dynamic(dependField = "type", condition = "value === 'SCORE'",
                            match = Dynamic.Ctrl.NOTNULL))
    )
    private String levels;

    @Transient
    @EruptField(
            edit = @Edit(title = "Means Yes", desc = "Optional: what a yes stands for",
                    dynamic = @Dynamic(dependField = "type", condition = "value === 'NOUL'"))
    )
    private String trueMeans;

    @Transient
    @EruptField(
            edit = @Edit(title = "Means No", desc = "Optional: what a no stands for",
                    dynamic = @Dynamic(dependField = "type", condition = "value === 'NOUL'"))
    )
    private String falseMeans;

    // What is actually sent to the provider, assembled from the editor the type calls for
    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Criteria")
    )
    private String criteria;

    /** Folds the type's editor into the stored json; called before the row is written */
    public void pack() {
        this.criteria = switch (type) {
            case CHOICE -> StringUtils.trimToNull(options);
            case SCORE -> StringUtils.trimToNull(levels);
            case NOUL -> {
                JsonObject json = new JsonObject();
                if (StringUtils.isNotBlank(trueMeans)) json.addProperty(YES, trueMeans);
                if (StringUtils.isNotBlank(falseMeans)) json.addProperty(NO, falseMeans);
                yield json.size() == 0 ? null : json.toString();
            }
        };
    }

    /** Spreads the stored json back over the editors; called before the row is shown */
    public void unpack() {
        options = null;
        levels = null;
        trueMeans = null;
        falseMeans = null;
        if (StringUtils.isBlank(criteria)) return;
        switch (type) {
            case CHOICE -> options = criteria;
            case SCORE -> levels = criteria;
            case NOUL -> {
                JsonElement json = read(criteria);
                if (json.isJsonObject()) {
                    trueMeans = text(json.getAsJsonObject(), YES);
                    falseMeans = text(json.getAsJsonObject(), NO);
                }
            }
        }
    }

    /** The runtime question this definition describes */
    public Question<?> toQuestion() {
        return Question.of(type, read(instructions), StringUtils.isBlank(criteria) ? null : read(criteria));
    }

    private static String text(JsonObject json, String key) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : null;
    }

    /** A field may hold either prose or json; json is passed through as structure, prose as a string */
    public static JsonElement read(String value) {
        try {
            JsonElement json = JsonParser.parseString(value);
            return json.isJsonObject() || json.isJsonArray() ? json : new JsonPrimitive(value);
        } catch (Exception e) {
            return new JsonPrimitive(value);
        }
    }

}
