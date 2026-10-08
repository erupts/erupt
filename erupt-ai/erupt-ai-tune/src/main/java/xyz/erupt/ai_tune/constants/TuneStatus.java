package xyz.erupt.ai_tune.constants;

import java.util.Set;

/**
 * Lifecycle of a fine-tuning job. Mirrors the stages every hosted platform exposes
 * (OpenAI, Together, DashScope...) so one status column serves all providers.
 *
 * @author YuePeng
 * date 2026/10/8
 */
public final class TuneStatus {

    private TuneStatus() {
    }

    // Created locally, nothing sent to the provider yet
    public static final String DRAFT = "DRAFT";

    // Dataset is being serialised and uploaded
    public static final String UPLOADING = "UPLOADING";

    // The provider is checking the uploaded files
    public static final String VALIDATING = "VALIDATING";

    public static final String QUEUED = "QUEUED";

    public static final String RUNNING = "RUNNING";

    public static final String SUCCEEDED = "SUCCEEDED";

    public static final String FAILED = "FAILED";

    public static final String CANCELLED = "CANCELLED";

    // Still moving: the scheduler keeps polling these
    public static final Set<String> ACTIVE = Set.of(UPLOADING, VALIDATING, QUEUED, RUNNING);

    // Can be (re)started from here
    public static final Set<String> STARTABLE = Set.of(DRAFT, FAILED, CANCELLED);

    // Provider accepts a cancel request in these states
    public static final Set<String> CANCELLABLE = Set.of(VALIDATING, QUEUED, RUNNING);

    public static boolean isTerminal(String status) {
        return SUCCEEDED.equals(status) || FAILED.equals(status) || CANCELLED.equals(status);
    }

}
