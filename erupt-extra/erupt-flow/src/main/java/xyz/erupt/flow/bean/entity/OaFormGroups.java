package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OaFormGroups implements Serializable {
    private static final long serialVersionUID = -40467384325438214L;

    @TableId(type = IdType.AUTO)
    /**
    * 分组ID
    */
    private Long groupId;
    /**
    * 分组名称
    */
    private String groupName;
    /**
    * 排序
    */
    private Integer sort;

    /**
    * 更新时间
    */
    private Date updated;

    @TableField(exist = false)
    private List<OaForms> items;

    @TableField(exist = false)
    private List<OaProcessDefinition> processDefs;

    //搜索的关键字，实际上作用于本分组下的流程，而非分组本身
    @TableField(exist = false)
    private String keywords;

}
