package xyz.erupt.ai_dev.worktree;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_dev.scope.DevProjectScope;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;

/**
 * A worktree of the project in the page header, read live from git rather than stored: each repository's
 * bare clone is asked for its worktrees, and only the ones under the project's own directory are shown.
 * Deleting one removes its directory and registration, never uncommitted work.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Erupt(name = "Worktree", primaryKeyCol = "path",
        power = @Power(add = false, edit = false, copy = false, importable = false, cellEdit = false, batchEdit = false))
@EruptScope(value = DevProjectScope.class, bind = false)
@EruptDataProcessor(DevWorktreeDataService.DATA_PROCESSOR)
@Getter
@Setter
@EruptI18n
public class DevWorktree {

    @EruptField(views = @View(title = "Path"))
    private String path;

    @EruptField(
            views = @View(title = "Requirement"),
            edit = @Edit(title = "Requirement", search = @Search)
    )
    private String requirement;

    @EruptField(
            views = @View(title = "Alias"),
            edit = @Edit(title = "Alias", search = @Search)
    )
    private String alias;

    @EruptField(views = @View(title = "Branch"))
    private String branch;

    @EruptField(views = @View(title = "Upstream"))
    private String upstream;

    @EruptField(views = @View(title = "Head"))
    private String head;

    @EruptField(views = @View(title = "Ahead"))
    private Integer ahead;

    @EruptField(views = @View(title = "Behind"))
    private Integer behind;

    @EruptField(views = @View(title = "Changed Files"))
    private Integer changed;

    @EruptField(views = @View(title = "Untracked Files"))
    private Integer untracked;

    // directory deleted by hand: git keeps the registration until pruned
    @EruptField(views = @View(title = "Missing"))
    private Boolean missing;

}
