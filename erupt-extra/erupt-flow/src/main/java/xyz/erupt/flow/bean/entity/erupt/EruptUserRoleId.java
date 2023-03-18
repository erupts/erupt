package xyz.erupt.flow.bean.entity.erupt;

import lombok.Data;

import java.io.Serializable;

@Data
public class EruptUserRoleId implements Serializable {

    private Long userId;

    private Long roleId;
}
