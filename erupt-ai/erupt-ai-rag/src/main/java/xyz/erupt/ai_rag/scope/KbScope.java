package xyz.erupt.ai_rag.scope;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;

/**
 * The knowledge base picked in the page header. Every model declaring it is filtered to that
 * knowledge base and new records land in it. The options are re-checked on every request.
 * <p>
 * No default: knowledge bases are shared, so none of them is "the natural one" to open on — the
 * page asks until the user picks, and the pick is remembered per account.
 *
 * @author YuePeng
 */
@Erupt(name = "Current Knowledge Base", primaryKeyCol = "kb")
@Getter
@Setter
@EruptI18n
public class KbScope {

    @EruptField(
            edit = @Edit(title = "Knowledge Base", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = KbFetch.class))
    )
    private Long kb;

}
