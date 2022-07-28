package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.ShowBy;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author YuePeng
 * date 2021/12/30 20:26
 */
@Entity
@Table(name = "e_bi_tpl", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@Erupt(name = "组件模板", orderBy = "createTime desc", dataProxy = BiTpl.class)
@Getter
@Setter
@EruptI18n
@Component
public class BiTpl extends MetaModelUpdateVo implements DataProxy<BiTpl> {

    public static final String TYPE_ONLINE = "online";

    public static final String TYPE_PATH = "path";

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "编码", width = "100px")
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "资源类型"),
            edit = @Edit(title = "资源类型", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(label = "文件路径", value = TYPE_PATH),
                                    @VL(label = "在线配置", value = TYPE_ONLINE)
                            }
                    ))
    )
    private String type = BiTpl.TYPE_ONLINE;

    @EruptField(
            views = @View(title = "路径"),
            edit = @Edit(title = "路径",
                    showBy = @ShowBy(dependField = "type", expr = "value == '" + BiTpl.TYPE_PATH + "'"),
                    desc = "resources路径下模板文件")
    )
    private String path;

    @EruptField(
            views = @View(title = "模板"),
            edit = @Edit(title = "模板", desc = "语法Freemarker",
                    showBy = @ShowBy(dependField = "type", expr = "value == '" + BiTpl.TYPE_ONLINE + "'"),
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "html"))
    )
    private String tpl;

    @Override
    public void beforeAdd(BiTpl biTpl) {
        biTpl.setCode(Erupts.generateCode());

    }

    @Override
    public void beforeUpdate(BiTpl biTpl) {
//        if (TYPE_ONLINE.equals(biTpl.getType())&& StringUtils.isBlank(biTpl.getTpl())) {
//            throw new EruptWebApiRuntimeException("template code i");
//        }
    }
}
