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
@Table(name = "e_bi_function", uniqueConstraints = @UniqueConstraint(name = "uk_bi_function_code", columnNames = "code"))
@Erupt(name = "Function Manager", dataProxy = BiFunctionDataProxy.class)
@Getter
@Setter
@Service
@EruptI18n
public class BiFunction extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Code", sortable = true, width = "120px"),
            edit = @Edit(title = "Code", search = @Search, show = false)
    )
    private String code;

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", notNull = true, search = @Search)
    )
    private String name;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Function Expression"),
            edit = @Edit(title = "Function Expression", desc = "Follow JavaScript function syntax",
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
