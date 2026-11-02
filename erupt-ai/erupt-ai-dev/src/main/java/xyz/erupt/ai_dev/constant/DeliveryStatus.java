package xyz.erupt.ai_dev.constant;

/**
 * Outcome of delivering one requirement to one repository.
 *
 * @author YuePeng
 * date 2026/10/21
 */
public enum DeliveryStatus {

    PENDING,
    PUSHED,
    // the repository was not changed: nothing to push
    SKIPPED,
    // rebasing onto the target branch conflicted: back to the agent
    CONFLICT,
    // refused by the remote, typically a protected branch
    REJECTED,
    FAILED

}
