package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_history")
@Erupt(
        name = "报表历史记录",
        orderBy = "operateTime desc",
        power = @Power(edit = false, add = false, viewDetails = false)
)
@Getter
@Setter
@EruptI18n
public class BiHistory extends BaseModel {

    @EruptField(
            views = @View(title = "操作人", column = "name")
    )
    @ManyToOne
    private EruptUserVo operateUser;

    @EruptField(
            views = @View(title = "操作时间", sortable = true)
    )
    private Date operateTime;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "SQL语句"),
            edit = @Edit(title = "SQL语句", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;


    @ManyToOne
    private Bi bi;
}
