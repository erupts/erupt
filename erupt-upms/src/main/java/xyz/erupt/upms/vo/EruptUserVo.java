package xyz.erupt.upms.vo;

import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptOrg;
import xyz.erupt.upms.model.EruptPost;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
public class EruptUserVo {

    private String account;

    private String name;

    private Boolean status;

    private String phone;

    private String email;

    private EruptMenu eruptMenu;

    private EruptOrg eruptOrg;

    private EruptPost eruptPost;

    private Boolean isMd5;

    private String whiteIp;

    private Boolean isAdmin;

}
