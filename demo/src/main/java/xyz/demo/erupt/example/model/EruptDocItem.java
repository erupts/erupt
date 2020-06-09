package xyz.demo.erupt.example.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.auth.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2020-05-22
 */
@Entity
@Getter
@Setter
@Table(name = "erupt_doc_item")
@Erupt(
        name = "erupt文档编辑页面",
        tree = @Tree(pid = "parent.id")
)
public class EruptDocItem extends BaseModel {

    @ManyToOne
    @EruptField
    private EruptDoc eruptDoc;

    @EruptField(
            views = @View(title = "锚点名称"),
            edit = @Edit(title = "锚点名称", notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "顺序"),
            edit = @Edit(title = "顺序", notNull = true)
    )
    private Integer sort;

    @Lob
    @EruptField(
            views = @View(title = "内容"),
            edit = @Edit(title = "内容", type = EditType.HTML_EDIT, notNull = true)
    )
    private String content;

}
