package xyz.erupt.bi.model;

import lombok.Getter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.auth.model.base.HyperModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI_CLASS_HANDLER")
@Erupt(name = "报表处理类")
@Getter
@Service
public class BiClassHandler extends HyperModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "处理类路径"),
            edit = @Edit(title = "处理类路径", notNull = true, search = @Search(vague = true),
                    desc = "需实现xyz.erupt.bi.fun.BiHandler接口")
    )
    private String handlerPath;

    @Lob
    @EruptField(
            views = @View(title = "参数"),
            edit = @Edit(title = "参数", type = EditType.TEXTAREA)
    )
    private String param;

    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

}
