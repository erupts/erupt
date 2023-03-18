package xyz.erupt.flow.bean.entity.node;

import lombok.Data;

@Data
public class OaProcessNodeFormPerms {

    /**
     * 字段id
     */
    String id;
    /**
     * 字段权限
     */
    String perm;
    /**
     * 标题
     */
    String title;
    /**
     * 是否必填
     */
    boolean required;
}
