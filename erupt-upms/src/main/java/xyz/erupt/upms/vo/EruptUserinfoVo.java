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

    /** Login account */
    private String account;

    /** Avatar path */
    private String avatar;

    /** Display name */
    private String nickname;

    /** Type of the menu opened as home page */
    private String indexMenuType;

    /** Value of the menu opened as home page */
    private String indexMenuValue;

    /** Whether the user must change the password */
    private boolean resetPwd = false;

    /** Organization code */
    private String org;

    /** Post code */
    private String post;

    /** Role codes */
    private List<String> roles;

}
