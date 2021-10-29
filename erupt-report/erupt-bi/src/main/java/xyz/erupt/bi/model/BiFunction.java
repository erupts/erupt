package xyz.erupt.bi.model;

import lombok.Getter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.service.BiDataInitService;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.upms.helper.HyperModelCreatorVo;

import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_function")
@Erupt(name = "报表函数", dataProxy = BiFunction.class)
@Getter
@Service
@EruptI18n
public class BiFunction extends HyperModelCreatorVo implements DataProxy<BiFunction> {

    @EruptField(
            views = @View(title = "编码", sortable = true),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
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

    @Resource
    @Transient
    private BiDataInitService biDataInitService;

    private static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(BiConst.SCRIPT_ENGINE);

    private void testFunction(BiFunction biFunction) {
        try {
            scriptEngine.eval(biFunction.getJsFunction());
        } catch (ScriptException e) {
            throw new EruptApiErrorTip(e.getMessage());
        }
    }

    @Override
    public void afterAdd(BiFunction biFunction) {
        this.testFunction(biFunction);
        biDataInitService.flushFunction();
    }

    @Override
    public void afterUpdate(BiFunction biFunction) {
        this.testFunction(biFunction);
        biDataInitService.flushFunction();
    }

    @Override
    public void afterDelete(BiFunction biFunction) {
        this.testFunction(biFunction);
        biDataInitService.flushFunction();
    }
}
