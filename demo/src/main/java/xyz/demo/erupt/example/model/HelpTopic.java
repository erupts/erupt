package xyz.demo.erupt.example.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.core.annotation.EruptDataSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2020-08-16
 */
@Entity
@Getter
@Setter
@Table(name = "help_topic")
@EruptDataSource("mysql")
@Erupt(
        name = "使用帮助",
        primaryKeyCol = "helpTopicId"
)
public class HelpTopic {

    @Id
    @EruptField
    @Column(name = "help_topic_id")
    private Integer helpTopicId;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称")
    )
    private String name;

    @EruptField(
            views = @View(title = "描述", sortable = true, type = ViewType.HTML),
            edit = @Edit(title = "描述")
    )
    private String description;

    @EruptField(
            views = @View(title = "示例", sortable = true, type = ViewType.HTML),
            edit = @Edit(title = "示例")
    )
    private String example;

    @EruptField(
            views = @View(title = "url", sortable = true, type = ViewType.QR_CODE),
            edit = @Edit(title = "url")
    )
    private String url;

}
