package xyz.erupt.ai_dev.constant;

/**
 * Lifecycle of a requirement. Stages move forward through the pool's own actions, never by editing the
 * record.
 *
 * @author YuePeng
 * date 2026/10/21
 */
public enum RequirementStage {

    DRAFT,
    // waiting for a reviewer to approve the definition
    REVIEW,
    // approved, waiting for an execution slot
    QUEUED,
    // the developer agent is working in the worktrees
    RUNNING,
    // the QA agent checks the change against the acceptance criteria
    VERIFYING,
    // a human reviews the diff before anything is pushed
    ACCEPTANCE,
    // pushed to every repository it touches
    DELIVERED,
    // the agent run failed or was stopped; retrying resumes its session
    FAILED,
    CANCELLED

}
