package xyz.erupt.core.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
public class BaseTree<T> extends BaseModel {


    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true
            )
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    @EruptField(
            edit = @Edit(
                    title = "父级节点",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "id")
            )
    )
    private T parent;

}
