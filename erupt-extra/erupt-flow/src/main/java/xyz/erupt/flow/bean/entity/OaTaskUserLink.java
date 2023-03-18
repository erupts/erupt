package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

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
public class OaTaskUserLink extends BaseModel {

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
