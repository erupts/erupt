package xyz.erupt.ai_claw.skill;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;

/**
 * Read-only view of the skill library (~/.erupt/skills), one row per live skill
 * directory. Rows are built on the fly by SkillDataService from the filesystem;
 * there is no backing table, so the page always reflects what the agent has
 * actually accumulated on disk.
 *
 * @author YuePeng
 * date 2026/8/28
 */
@Erupt(
        name = "Skill",
        primaryKeyCol = "name",
        power = @Power(add = false, edit = false, delete = false, export = false)
)
@EruptDataProcessor(SkillDataService.DATA_PROCESSOR)
@EruptI18n
@Getter
@Setter
public class EruptSkill {

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description", search = @Search(operator = QueryExpression.LIKE))
    )
    private String description;

    @EruptField(
            views = @View(title = "Pinned", type = ViewType.BOOLEAN, sortable = true),
            edit = @Edit(title = "Pinned", type = EditType.BOOLEAN, search = @Search)
    )
    private Boolean pinned;

    @EruptField(
            views = @View(title = "Uses", sortable = true),
            edit = @Edit(title = "Uses", type = EditType.NUMBER)
    )
    private Integer uses;

    @EruptField(
            views = @View(title = "Patches", sortable = true),
            edit = @Edit(title = "Patches", type = EditType.NUMBER)
    )
    private Integer patches;

    @EruptField(
            views = @View(title = "Last Used", sortable = true),
            edit = @Edit(title = "Last Used")
    )
    private String lastUsed;

    // Populated only by findDataById so the detail view shows the full SKILL.md without bloating list payloads
    @EruptField(
            edit = @Edit(title = "SKILL.md", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown", height = 500))
    )
    private String content;

}
