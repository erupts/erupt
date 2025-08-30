package xyz.erupt.bi.model;

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
import xyz.erupt.bi.handler.BiHandlerChoice;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_class_handler")
@Erupt(name = "报表处理类")
@Getter
@Service
@EruptI18n
public class BiClassHandler extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "处理类", sortable = true),
            edit = @Edit(title = "处理类", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = BiHandlerChoice.class),
                    desc = "实现xyz.erupt.bi.fun.EruptBiHandler接口")
    )
    private String handlerPath;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "处理类参数"),
            edit = @Edit(title = "处理类参数", desc = "可在处理类exprHandler param参数中获取", type = EditType.CODE_EDITOR)
    )
    private String param;

    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

}
