package xyz.erupt.sample.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;
import xyz.erupt.file.service.EruptFileDataService;

/**
 * File data source in single-record mode: a pure settings form persisted to a Java
 * properties file. The whole file is one record — add seeds it, edit overwrites it,
 * delete clears it, with no primary-key bookkeeping. {@link FileType#PROPERTIES} is
 * set explicitly because the {@code .conf} extension would otherwise be inferred as
 * JSON. The fixed {@code id} keeps the single row addressable without showing up in
 * the form.
 *
 * @author YuePeng
 */
@Erupt(name = "Site Setting (single record)", power = @Power(export = false, importable = false))
@EruptDataProcessor(EruptFileDataService.DATA_PROCESSOR)
@EruptFile(value = "data/site-setting.conf", type = FileType.PROPERTIES, single = true)
@Getter
@Setter
public class SiteSetting {

    // never surfaced as a field; anchors the single record for edit / delete
    @EruptField
    private Integer id = 1;

    @EruptField(
            views = @View(title = "Site Name"),
            edit = @Edit(title = "Site Name", notNull = true)
    )
    private String siteName;

    @EruptField(
            views = @View(title = "Copyright"),
            edit = @Edit(title = "Copyright")
    )
    private String copyright;

    @EruptField(
            views = @View(title = "Records Per Page"),
            edit = @Edit(title = "Records Per Page", type = EditType.NUMBER)
    )
    private Integer pageSize;

    @EruptField(
            views = @View(title = "Maintenance Mode"),
            edit = @Edit(title = "Maintenance Mode", type = EditType.BOOLEAN)
    )
    private Boolean maintenance;

}
