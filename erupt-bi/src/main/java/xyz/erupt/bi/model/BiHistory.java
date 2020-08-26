package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.model.base.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI_HISTORY")
@Erupt(
        name = "报表历史记录",
        orderBy = "operateTime desc",
        power = @Power(edit = false, add = false, viewDetails = false)
)
@Getter
@Setter
public class BiHistory extends BaseModel {

    @Lob
    @EruptField(
            views = @View(title = "SQL语句"),
            edit = @Edit(title = "SQL语句", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

    @EruptField(
            views = @View(title = "操作时间")
    )
    private Date operateTime;

    @EruptField(
            views = @View(title = "操作人", column = "name")
    )
    @ManyToOne
    private EruptUser operateUser;

    @ManyToOne
    private Bi bi;
}
