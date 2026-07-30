package xyz.erupt.report.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.report.handler.ReportHandlerChoice;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_class_handler")
@Erupt(name = "Report Handler")
@Getter
@Service
@EruptI18n
public class BiClassHandler extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "Handler Class", sortable = true),
            edit = @Edit(title = "Handler Class", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = ReportHandlerChoice.class),
                    desc = "Implement xyz.erupt.bi.fun.EruptReportHandler interface")
    )
    private String handlerPath;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Handler Params"),
            edit = @Edit(title = "Handler Params", desc = "Accessible via exprHandler param in the handler", type = EditType.CODE_EDITOR)
    )
    private String param;

    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
