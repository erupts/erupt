package xyz.erupt.sample.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.service.EruptFileDataService;
import xyz.erupt.jpa.model.BaseModel;

/**
 * File data source in list mode: a data dictionary stored as CSV. Each row is a
 * line in {@code data/dict.csv}; the file (and its folder) is created on the first
 * insert. The primary key is auto-generated (max + 1) like a table. Good for
 * config / dictionary scale data that a human may also edit by hand.
 *
 * @author YuePeng
 */
@Erupt(name = "File Dictionary (CSV)", power = @Power(export = true, importable = true))
@EruptDataProcessor(EruptFileDataService.DATA_PROCESSOR)
@EruptFile("data/dict.csv")
@Getter
@Setter
public class FileDict extends BaseModel {

    @EruptField(
            views = @View(title = "Code"),
            edit = @Edit(title = "Code", search = @Search, notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "Label"),
            edit = @Edit(title = "Label", notNull = true)
    )
    private String label;

    @EruptField(
            views = @View(title = "Sort", sortable = true),
            edit = @Edit(title = "Sort")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "Enabled"),
            edit = @Edit(title = "Enabled", search = @Search)
    )
    private Boolean enabled;

}
