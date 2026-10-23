package xyz.erupt.ai_dev.constant;

/**
 * @author YuePeng
 * date 2026/10/21
 */
public enum MemberRole {

    // manages the project: settings, repositories, members
    OWNER,
    MAINTAINER,
    // approves requirements and deliveries
    REVIEWER,
    MEMBER;

    public boolean manages() {
        return this == OWNER || this == MAINTAINER;
    }

}
