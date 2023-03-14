package xyz.erupt.flow.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机构选择组件对象
 * 可以用于选机构，角色，人员等
 * @version : 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgTreeVo {

    private String id;

    private String name;

    //user-选人  dept-选部门 role-选角色
    private String type;

    private String avatar;

    private Boolean sex;

    private Boolean selected;
}
