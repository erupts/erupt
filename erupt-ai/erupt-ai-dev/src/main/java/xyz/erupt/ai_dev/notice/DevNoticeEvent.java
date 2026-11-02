package xyz.erupt.ai_dev.notice;

/**
 * A requirement reached a stage someone has to hear about. Published inside the transaction that moved
 * it and handled after that commits, so nobody is told about a change that rolled back.
 *
 * @param tenant        tenant of the requirement
 * @param requirementId the requirement
 * @param kind          what happened
 * @param actor         user who did it, never notified of their own action; null for the executor
 * @author YuePeng
 * date 2026/10/22
 */
public record DevNoticeEvent(String tenant, Long requirementId, Kind kind, Long actor) {

    public enum Kind {
        // waiting for review: the assigned reviewer, else every reviewer of the project
        SUBMITTED,
        // review outcome: the creator
        APPROVED, REJECTED,
        // agents done, waiting for acceptance: reviewer and creator
        ACCEPTANCE,
        // the run failed: creator and reviewer
        FAILED,
        // pushed: creator and reviewer
        DELIVERED
    }

}
