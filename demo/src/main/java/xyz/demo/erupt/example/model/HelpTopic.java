package xyz.demo.erupt.example.model;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.core.annotation.EruptDataSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2020-09-01
 */
@EruptDataSource("mysql")
@Erupt(name = "多数据源表")
@Table(name = "help_topic")
@Entity
@Component
public class HelpTopic {

    @Id
    @Column(name = "help_topic_id")
    @EruptField()
    private String id;

    @EruptField(
            views = @View(title = "名称")
    )
    private String name;

    @EruptField(
            views = @View(title = "描述", type = ViewType.HTML)
    )
    private String description;

    @EruptField(
            views = @View(title = "实例", type = ViewType.HTML)
    )
    private String example;

    @EruptField(
            views = @View(title = "地址", type = ViewType.QR_CODE)
    )
    private String url;
}
