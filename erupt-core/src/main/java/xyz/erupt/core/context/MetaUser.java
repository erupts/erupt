package xyz.erupt.core.context;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/12/20 23:20
 */
@Getter
@Setter
public class MetaUser {

    private Long uid; // user ID

    private String account; // login username

    private String name; // user display name

    public MetaUser(Long uid, String account, String name) {
        this.uid = uid;
        this.account = account;
        this.name = name;
    }

    public MetaUser() {
    }
}
