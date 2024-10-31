package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2022/11/24 19:44
 */
@Getter
@Setter
public class EruptUserinfoVo {

    //用户名
    private String nickname;

    //用户首页菜单类型
    private String indexMenuType;

    //用户首页菜单值
    private String indexMenuValue;

    //是否需要重置密码
    private boolean resetPwd = false;

    //组织编码
    private String org;

    //岗位编码
    private String post;

    //角色列表
    private List<String> roles;

}
