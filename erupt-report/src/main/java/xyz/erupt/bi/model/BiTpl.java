package xyz.erupt.bi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2021/12/30 20:26
 */
@Entity
@Table(name = "e_bi_tpl", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@Erupt(name = "Component Template", orderBy = "createTime desc", dataProxy = BiTpl.class)
@Getter
@Setter
@EruptI18n
@Component
public class BiTpl extends MetaModelUpdateVo implements DataProxy<BiTpl> {

    public static final String TYPE_ONLINE = "online";

    public static final String TYPE_PATH = "path";

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Code", width = "100px")
    )
    private String code;

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Resource Type"),
            edit = @Edit(title = "Resource Type", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(label = "File Path", value = TYPE_PATH),
                                    @VL(label = "Online Config", value = TYPE_ONLINE)
                            }
                    ))
    )
    private String type = BiTpl.TYPE_ONLINE;

    @EruptField(
            views = @View(title = "Path"),
            edit = @Edit(title = "Path",
                    dynamic = @Dynamic(dependField = "type", condition = "value == '" + BiTpl.TYPE_PATH + "'", match = Dynamic.Ctrl.NOTNULL),
                    desc = "Template file under resources path")
    )
    private String path;

    @EruptField(
            views = @View(title = "Template"),
            edit = @Edit(title = "Template", desc = "Freemarker syntax",
                    dynamic = @Dynamic(dependField = "type", condition = "value == '" + BiTpl.TYPE_ONLINE + "'", match = Dynamic.Ctrl.NOTNULL),
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "html"))
    )
    private String tpl;

    @Override
    public void beforeAdd(BiTpl biTpl) {
        biTpl.setCode(Erupts.generateCode());

    }

}
