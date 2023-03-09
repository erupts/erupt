package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OaForms implements Serializable {
    private static final long serialVersionUID = -40467384325438214L;

    /**
    * 表单ID
    */
    @TableId(type = IdType.AUTO)
    private Long formId;
    /**
    * 表单名称
    */
    private String formName;
    /**
    * 图标配置
    */
    private String logo;
    /**
    * 设置项
    */
    private String settings;
    /**
    * 分组ID
    */
    private Long groupId;
    /**
    * 表单设置内容
    */
    private String formItems;
    /**
    * 流程设置内容
    */
    private String process;
    /**
    * 备注
    */
    private String remark;
    /**
     * 状态 0=正常 1=已停用
     */
    private Boolean isStop;

    private Integer sort;
    /**
    * 创建时间
    */
    private Date created;
    /**
    * 更新时间
    */
    private Date updated;
}
