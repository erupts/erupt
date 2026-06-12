package xyz.erupt.designer.template;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

import java.util.Date;
import java.util.Set;

/**
 * Annotation template for the form designer.
 * <p>
 * Provides raw annotation instances which are proxied at runtime with designer json values
 * (see {@link xyz.erupt.designer.proxy.JsonAnnotationProxy}). Annotation array members that the
 * designer can fill (views / vl) declare one prototype element to proxy from.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Getter
@Setter
@Erupt(name = "EruptDesignerTemplate")
public class EruptDesignerTemplate {

    @EruptField(
            views = @View(title = ""),
            edit = @Edit(title = "",
                    choiceType = @ChoiceType(vl = @VL(value = "", label = "")),
                    multiChoiceType = @MultiChoiceType(vl = @VL(value = "", label = "")))
    )
    private String stringValue;

    @EruptField(views = @View(title = ""), edit = @Edit(title = ""))
    private Integer numberValue;

    @EruptField(views = @View(title = ""), edit = @Edit(title = ""))
    private Boolean boolValue;

    @EruptField(views = @View(title = ""), edit = @Edit(title = ""))
    private Date dateValue;

    @EruptField(views = @View(title = ""), edit = @Edit(title = ""))
    private Object refValue;

    @EruptField(views = @View(title = ""), edit = @Edit(title = ""))
    private Set<Object> setValue;

}
