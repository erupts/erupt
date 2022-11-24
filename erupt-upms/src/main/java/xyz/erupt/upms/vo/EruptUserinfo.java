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
public class EruptUserinfo {


    //用户菜单
    private List<EruptMenuVo> eruptMenuVos;

    //用户名
    private String nickname;

    //用户首页菜单类型
    private String indexMenuType;

    //用户首页菜单值
    private String indexMenuValue;

    //是否强制重置密码
    private boolean resetPwd = false;

}
