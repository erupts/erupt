#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};
#end

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Erupt(name = "${NAME}")
@Table(name = "t_${NAME}")
@Entity
@Getter
@Setter
public class ${NAME} extends BaseModel {

    @EruptField(
            views = @View(title = ""),
            edit = @Edit(title = "", search = @Search(vague = true))
    )
    private String field;

}
