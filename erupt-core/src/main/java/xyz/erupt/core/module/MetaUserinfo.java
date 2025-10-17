package xyz.erupt.core.module;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2021/11/29 21:03
 */
@Getter
@Setter
public class MetaUserinfo {

    private Long id;

    private String account;

    private String username;

    private String org;

    private String post;

    private boolean superAdmin; // Is it a super administrator?

    private List<String> roles; // Character List

    private String tenantId;

}
