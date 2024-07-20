package xyz.erupt.flow.bean.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "oa_re_form_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OaFormGroups implements Serializable {
    private static final long serialVersionUID = -40467384325438214L;

    /**
     * 分组ID
     */
    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
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
    private List<OaForms> items;

    @Transient
    private List<OaProcessDefinition> processDefs;

    //搜索的关键字，实际上作用于本分组下的流程，而非分组本身
    @Transient
    private String keywords;

}
