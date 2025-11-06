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

    private String avatar;

    private String nickname;

    // Type of menu on the user's home page
    private String indexMenuType;

    // User home page menu value
    private String indexMenuValue;

    // Do you need to reset your password?
    private boolean resetPwd = false;

    // Organizational code
    private String org;

    // Position code
    private String post;

    // Character List
    private List<String> roles;

}
