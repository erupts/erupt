package xyz.erupt.ai_dev.scope;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;

/**
 * The project picked in the page header. Every model declaring it is filtered to that project and new
 * records land in it. The options are the user's own projects, re-checked on every request, so the
 * header cannot reach a project the user is not a member of.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Erupt(name = "Current Project", primaryKeyCol = "project", dataProxy = ProjectScopeProxy.class)
@Getter
@Setter
@EruptI18n
public class DevProjectScope {

    @EruptField(
            edit = @Edit(title = "Project", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = MemberProjectFetch.class))
    )
    private Long project;

}
