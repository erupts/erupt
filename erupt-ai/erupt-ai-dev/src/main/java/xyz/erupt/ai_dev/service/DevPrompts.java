package xyz.erupt.ai_dev.service;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The prompts an agent run starts from. Progress and the QA verdict come back as plain marker lines the
 * agent prints, so any CLI can report them.
 *
 * @author YuePeng
 * date 2026/10/22
 */
public final class DevPrompts {

    // PROGRESS 40 implement
    public static final Pattern PROGRESS = Pattern.compile("(?m)^\\s*PROGRESS\\s+(\\d{1,3})\\s*(.{0,80})$");

    // VERDICT: PASS | VERDICT: FAIL <reason>
    public static final Pattern VERDICT = Pattern.compile("(?m)^\\s*VERDICT:\\s*(PASS|FAIL)\\b(.*)$");

    private DevPrompts() {
    }

    public static String developer(DevRunContext ctx) {
        return """
                You are the developer agent of the project "%s". Implement the requirement below.

                Your working directory holds the project's repositories, one directory each: %s.
                Each one is a git worktree already checked out on the branch %s.

                ## Requirement %s: %s

                %s
                %s
                ## Rules
                - Work only inside this directory. Do not switch branches, push, or touch anything outside it.
                - Commit your work in every repository you change, with a message that ends with the line
                  "Requirement: %s".
                - Report progress with the erupt tool devReportProgress (percent, phase) whenever you enter a new phase:
                  assess, design, implement, verify. Without that tool, print a line of its own instead:
                  PROGRESS <0-100> <phase>, for example "PROGRESS 40 implement".
                - Finish with a short summary: what you changed in which repository, and how you verified it.
                """.formatted(ctx.projectName(), aliases(ctx), ctx.workBranch(), ctx.code(), ctx.title(),
                StringUtils.defaultIfBlank(ctx.description(), "(no description)"), instruction(ctx), ctx.code());
    }

    /**
     * A retry continues the previous session, which already holds the requirement.
     */
    public static String resume(DevRunContext ctx) {
        return """
                Continue requirement %s where you left off.
                %s
                Keep following the rules from the start of this session, and finish with a short summary.
                """.formatted(ctx.code(), instruction(ctx));
    }

    public static String qa(DevRunContext ctx) {
        return """
                You are the QA agent of the project "%s". The developer agent has implemented the requirement below
                on the branch %s in these repositories: %s. Verify it.

                ## Requirement %s: %s

                %s

                ## Rules
                - Do not modify, commit or push anything: you only inspect and run checks.
                - Read the change with git log / git diff against the base branch, and run the tests that cover it.
                - Check every acceptance criterion of the requirement.
                - Report the outcome with the erupt tool devReportVerdict (pass, reason), then end with exactly one line:
                  "VERDICT: PASS", or "VERDICT: FAIL <reason>".
                """.formatted(ctx.projectName(), ctx.workBranch(), aliases(ctx), ctx.code(), ctx.title(),
                StringUtils.defaultIfBlank(ctx.description(), "(no description)"));
    }

    /**
     * The last verdict line of a QA answer, e.g. "FAIL the export ignores the date filter"; null when none.
     */
    public static String verdict(String text) {
        if (null == text) return null;
        Matcher m = VERDICT.matcher(text);
        String verdict = null;
        while (m.find()) verdict = (m.group(1) + " " + m.group(2).trim()).trim();
        return verdict;
    }

    private static String aliases(DevRunContext ctx) {
        return ctx.repos().stream().map(DevRunContext.Repo::alias).collect(Collectors.joining(", "));
    }

    private static String instruction(DevRunContext ctx) {
        return StringUtils.isBlank(ctx.instruction()) ? "" : "\n## Additional instruction\n\n" + ctx.instruction().trim() + "\n";
    }

}
