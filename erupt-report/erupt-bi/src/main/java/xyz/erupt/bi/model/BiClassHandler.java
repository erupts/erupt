package xyz.erupt.bi.model;

import lombok.Getter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.helper.HyperModelCreatorVo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
public class BiClassHandler extends HyperModelCreatorVo {

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "处理类路径", sortable = true),
            edit = @Edit(title = "处理类路径", notNull = true,
                    desc = "需实现xyz.erupt.bi.fun.EruptBiHandler接口", inputType = @InputType(fullSpan = true))
    )
    private String handlerPath;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "自定义参数"),
            edit = @Edit(title = "自定义参数", type = EditType.TEXTAREA)
    )
    private String param;

    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

}
