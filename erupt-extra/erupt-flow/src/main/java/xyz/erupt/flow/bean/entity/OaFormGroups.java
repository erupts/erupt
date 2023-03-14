package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "oa_re_form_groups")
@TableName("oa_re_form_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OaFormGroups implements Serializable {
    private static final long serialVersionUID = -40467384325438214L;

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
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

    @Transient
    @TableField(exist = false)
    private List<OaForms> items;

    @Transient
    @TableField(exist = false)
    private List<OaProcessDefinition> processDefs;

    //搜索的关键字，实际上作用于本分组下的流程，而非分组本身
    @TableField(exist = false)
    private String keywords;

}
