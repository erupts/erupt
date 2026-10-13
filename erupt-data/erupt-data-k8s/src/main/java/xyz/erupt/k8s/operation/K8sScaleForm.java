package xyz.erupt.k8s.operation;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;

/**
 * Form of {@link K8sScaleHandler}.
 *
 * @author YuePeng
 */
@Getter
@Setter
// A form, not a table: the only field doubles as the key erupt requires
@Erupt(name = "Scale", primaryKeyCol = "replicas")
public class K8sScaleForm {

    @EruptField(edit = @Edit(title = "Replicas", notNull = true, type = EditType.NUMBER, numberType = @NumberType(min = 0)))
    private Integer replicas;

}
