package xyz.erupt.bi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Date;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_history", indexes = @Index(columnList = "bi_id"))
@Erupt(
        name = "Modification History",
        orderBy = "BiHistory.operateTime desc",
        power = @Power(edit = false, add = false, viewDetails = false)
)
@Getter
@Setter
@EruptI18n
public class BiHistory extends BaseModel {

    @EruptField(
            views = @View(title = "Source", width = "120px"),
            edit = @Edit(title = "Source", search = @Search)
    )
    private String mark;

    @EruptField(
            views = @View(title = "Operator"),
            edit = @Edit(title = "Operator", search = @Search)
    )
    private String operateBy;

    @EruptField(
            views = @View(title = "Operate Time", sortable = true, width = "200"),
            edit = @Edit(title = "Operate Time", search = @Search, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date operateTime;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Before"),
            edit = @Edit(title = "Before", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "After"),
            edit = @Edit(title = "After", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String afterSqlStatement;

    @Column(name = "bi_id")
    private Long biId;

}
