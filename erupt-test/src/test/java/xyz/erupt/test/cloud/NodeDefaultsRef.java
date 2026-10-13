package xyz.erupt.test.cloud;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

/**
 * Target of the reference fields and form of the row operation in {@link NodeDefaultsFixture}.
 */
@Getter
@Setter
@Erupt(name = "NodeDefaultsRef", authVerify = false)
public class NodeDefaultsRef {

    @EruptField
    private Long id;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-"))
    private String name;

}
