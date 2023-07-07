package xyz.erupt.flow.bean.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.service.impl.ProcessDefinitionServiceImpl;

import javax.persistence.*;
import java.util.Date;

/**
 * 流程定义，每个流程发布后，都会产生一个流程定义
 * 流程定义发布后就不能改变，重新发布只会产生新的版本
 */
@Erupt(name = "流程定义"
        , power = @Power(export = true, add = false, edit = false)
        , dataProxy = ProcessDefinitionServiceImpl.class
)
@Table(name = "oa_re_process_definition")
@TableName("oa_re_process_definition")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaProcessDefinition {

    @Id
    @Column(name = "id")
    @EruptField(views = @View(title = "流程定义ID", sortable = true))
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 继承自oa_forms，具有其全部字段
     */
    @EruptField(views = @View(title = "表单ID"))
    private Long formId;

    /**
     * 表单名称
     */
    @EruptField(
            views = @View(title = "表单名称")
            , edit = @Edit(title = "表单名称", search = @Search(vague = true))
    )
    private String formName;

    @EruptField(views = @View(title = "版本号"))
    private Integer version;//版本号，同一个OaForms多次发布，会产生多个流程定义，这些流程定义具有相同的formId，但版本号递增

    /**
     * 图标配置
     */
    @EruptField(views = @View(title = "图标", show = false))
    private String logo;
    /**
     * 设置项
     */
    @EruptField(views = @View(title = "设置项"))
    @Lob
    @Column//json类型
    private String settings;
    /**
     * 分组ID
     */
    @EruptField(views = @View(title = "分组ID", show = false))
    private Long groupId;

    @Transient//标识虚拟列
    @TableField(exist = false)
    @EruptField(views = @View(title = "分组"))
    private String groupName;

    /**
     * 流程设置内容
     */
    @Lob
    @Column//json类型
    private String process;

    /**
     * 表单内容
     */
    @Lob
    @Column//json类型
    private String formItems;

    public OaProcessNode getProcessNode() {
        if(this.getProcess()==null) {
            return null;
        }
        return JSON.parseObject(this.getProcess(), OaProcessNode.class);
    }
    /**
     * 备注
     */
    @EruptField(views = @View(title = "备注"))
    private String remark;
    /**
     * 状态 0=正常 1=已停用
     */
    @EruptField(views = @View(title = "是否停用"), edit = @Edit(title = "是否停用", search = @Search))
    private Boolean isStop;

    @EruptField
    private Integer sort;
    /**
     * 创建时间
     */
    @EruptField(views = @View(title = "创建时间"), edit = @Edit(title = "创建时间", search = @Search(vague = true)))
    private Date created;

}
