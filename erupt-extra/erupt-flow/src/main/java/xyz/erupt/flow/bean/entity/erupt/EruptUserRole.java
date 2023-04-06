package xyz.erupt.flow.bean.entity.erupt;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@IdClass(EruptUserRoleId.class)
@Table(name = "e_upms_user_role")
@Getter
@Setter
public class EruptUserRole {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "role_id")
    private Long roleId;
}
