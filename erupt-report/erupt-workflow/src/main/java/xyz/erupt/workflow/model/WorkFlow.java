package xyz.erupt.workflow.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.workflow.annotation.EruptWorkflow;

import javax.persistence.*;

/**
 * @author YuePeng
 * date 2022/8/17 22:12
 */
@EruptWorkflow
@Erupt(name = "工作流定义实体")
@Table(name = "e_workflow", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Entity
@Getter
@Setter
public class WorkFlow extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码")
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称")
    )
    private String name;

    @EruptField(
            views = @View(title = "表单模型"),
            edit = @Edit(title = "表单模型")
    )
    private String eruptModel;

    @Lob
    @EruptField(
            views = @View(title = "流程配置", type = ViewType.HTML)
    )
    private String config;

    @Column(length = 4000)
    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述", type = EditType.TEXTAREA)
    )
    private String remark;


}
