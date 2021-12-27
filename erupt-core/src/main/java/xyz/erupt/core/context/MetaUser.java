package xyz.erupt.core.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/12/20 23:20
 */
@Builder
@Getter
@Setter
public class MetaUser {

    private String uid; //用户id
    private String account; //登录用户名
    private String name; //用户姓名

    public MetaUser(String uid, String account, String name) {
        this.uid = uid;
        this.account = account;
        this.name = name;
    }
}
