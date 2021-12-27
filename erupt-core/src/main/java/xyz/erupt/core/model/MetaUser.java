package xyz.erupt.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/12/20 23:20
 */
@Getter
@Setter
public class MetaUser {

    private static final ThreadLocal<MetaUser> authUserThreadLocal = new ThreadLocal<>();
    private String uid; //用户id
    private String account; //登录用户名
    private String name; //用户姓名

    public MetaUser(String uid, String account, String name) {
        this.uid = uid;
        this.account = account;
        this.name = name;
    }

    public MetaUser() {
    }

    //注册当前用户对象
    public static MetaUser get() {
        MetaUser metaUser = authUserThreadLocal.get();
        return null == metaUser ? new MetaUser() : metaUser;
    }

    public static void register(MetaUser value) {
        authUserThreadLocal.set(value);
    }

    public static void remove() {
        authUserThreadLocal.remove();
    }
}
