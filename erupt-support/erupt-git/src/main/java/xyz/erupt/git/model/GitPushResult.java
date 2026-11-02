package xyz.erupt.git.model;

/**
 * Outcome of pushing one ref.
 *
 * @param detail git's summary for the ref, e.g. "[rejected] (non-fast-forward)"
 * @author YuePeng
 * date 2026/10/21
 */
public record GitPushResult(Status status, String detail) {

    public enum Status {
        PUSHED,
        UP_TO_DATE,
        // the remote moved on: fetch, rebase and push again
        NON_FAST_FORWARD,
        // refused by the remote itself, typically a protected branch
        REJECTED
    }

    public boolean ok() {
        return status == Status.PUSHED || status == Status.UP_TO_DATE;
    }

}
