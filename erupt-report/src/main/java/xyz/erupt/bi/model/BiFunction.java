package xyz.erupt.bi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.bi.model.dataproxy.BiFunctionDataProxy;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_function", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "函数管理", dataProxy = BiFunctionDataProxy.class)
@Getter
@Setter
@Service
@EruptI18n
public class BiFunction extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "编码", sortable = true, width = "120px"),
            edit = @Edit(title = "编码", search = @Search(vague = true), show = false)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "函数表达式"),
            edit = @Edit(title = "函数表达式", desc = "参照JavaScript function写法",
                    codeEditType = @CodeEditorType(language = "javascript"), notNull = true, type = EditType.CODE_EDITOR)
    )
    private String jsFunction;


    public BiFunction(String code, String name, String jsFunction) {
        this.code = code;
        this.name = name;
        this.jsFunction = jsFunction;
    }

    public BiFunction() {
    }

}
