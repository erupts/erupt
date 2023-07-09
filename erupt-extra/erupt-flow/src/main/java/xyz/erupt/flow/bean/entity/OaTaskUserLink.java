package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;

import javax.persistence.*;

/**
 * 用户和候选人的中间表
 */
@Erupt(name = "任务候选人 "
        , power = @Power(export = true, add = false, edit = false)
)
@Table(name = "oa_ru_task_user_link")
@TableName("oa_ru_task_user_link")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaTaskUserLink {

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    /**
     * 候选人类型
     * USERS 关联多个用户，这些人中任意人都可以处理
     * ROLES 关联多个角色，这些角色中任意人都可以处理此任务
     */
    private String userLinkType;

    private String linkId;//当候选人类型为USERS，表示用户id，当候选人类型为ROLES，表示角色id

    private String linkName;
}
