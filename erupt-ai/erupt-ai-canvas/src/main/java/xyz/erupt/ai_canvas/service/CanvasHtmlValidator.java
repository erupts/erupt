package xyz.erupt.ai_canvas.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static checks on a generated page before it is filed as a version. Browsers
 * forgive most HTML mistakes, so the checks target what actually leaves the
 * page blank: a document cut off by the token limit, markdown fences leaking
 * into the source, inline scripts with unbalanced brackets (a syntax error kills
 * the whole script and the Vue app with it), and framework globals used without
 * the script that defines them. Findings are phrased for the LLM, which gets one
 * repair round to fix them.
 *
 * @author YuePeng
 * date 2026/9/6
 */
public final class CanvasHtmlValidator {

    private CanvasHtmlValidator() {
    }

    private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

    private static final Pattern SCRIPT_BLOCK = Pattern.compile("<script\\b([^>]*)>(.*?)</script\\s*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern SRC_ATTR = Pattern.compile("\\bsrc\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);

    private static final Pattern TYPE_ATTR = Pattern.compile("\\btype\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);

    // Script types whose body is JavaScript; anything else (x-template, importmap, json) is skipped
    private static final Set<String> JS_TYPES = Set.of("", "text/javascript", "application/javascript", "module", "text/babel");

    // Identifiers after which a '/' starts a regex literal rather than a division
    private static final Set<String> REGEX_PRECEDING_KEYWORDS = Set.of(
            "return", "typeof", "case", "do", "else", "in", "of", "instanceof", "new", "delete", "void", "throw", "yield", "await");

    public static List<String> validate(String html) {
        List<String> problems = new ArrayList<>();
        String stripped = HTML_COMMENT.matcher(html).replaceAll("");
        String lower = stripped.toLowerCase(Locale.ROOT);
        if (!lower.contains("<html")) problems.add("no <html> element");
        if (!lower.contains("</html>")) problems.add("missing </html> — the document is incomplete, probably cut off");
        if (!lower.contains("<body")) problems.add("no <body> element");
        else if (!lower.contains("</body>")) problems.add("missing </body> — the document is incomplete, probably cut off");
        if (stripped.contains("```")) problems.add("a markdown code fence (```) leaked into the document; the output must be plain HTML inside ONE fenced block");
        tagParity(lower, "script", problems);
        tagParity(lower, "style", problems);
        tagParity(lower, "template", problems);
        // Bracket checks only make sense on a document that was not cut off
        if (problems.isEmpty()) {
            scripts(stripped, problems);
            assets(stripped, lower, problems);
        }
        return problems;
    }

    private static void tagParity(String lower, String tag, List<String> problems) {
        int open = count(lower, "<" + tag), close = count(lower, "</" + tag);
        if (open != close) {
            problems.add("<" + tag + "> tags are unbalanced (" + open + " opened, " + close + " closed) — the output is truncated or a tag is missing");
        }
    }

    private static int count(String haystack, String needle) {
        int n = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            // "<script" must not also count "<scripts"; the next char has to end the tag name
            int end = i + needle.length();
            if (end >= haystack.length() || !Character.isLetterOrDigit(haystack.charAt(end))) n++;
        }
        return n;
    }

    private static void scripts(String html, List<String> problems) {
        Matcher m = SCRIPT_BLOCK.matcher(html);
        int index = 0;
        while (m.find()) {
            index++;
            String attrs = m.group(1);
            if (SRC_ATTR.matcher(attrs).find()) continue;
            Matcher type = TYPE_ATTR.matcher(attrs);
            if (type.find() && !JS_TYPES.contains(type.group(1).trim().toLowerCase(Locale.ROOT))) continue;
            String problem = bracketProblem(m.group(2));
            if (null != problem) problems.add("inline <script> #" + index + " " + problem + " — a JavaScript syntax error, or the output was cut off");
        }
    }

    // Framework globals referenced without the script that defines them, and the
    // one ordering rule that matters: Element Plus is a Vue plugin, so vue3.js first
    private static void assets(String html, String lower, List<String> problems) {
        List<String> srcs = new ArrayList<>();
        Matcher m = SCRIPT_BLOCK.matcher(html);
        while (m.find()) {
            Matcher src = SRC_ATTR.matcher(m.group(1));
            if (src.find()) srcs.add(src.group(1).toLowerCase(Locale.ROOT));
        }
        int vue = indexOfSrc(srcs, "vue");
        int element = indexOfSrc(srcs, "element");
        boolean usesVue = Pattern.compile("\\bVue\\.").matcher(html).find() || Pattern.compile("\\bcreateApp\\s*\\(").matcher(html).find();
        boolean usesElement = html.contains("ElementPlus") || lower.contains("<el-") || Pattern.compile("\\bElMessage(Box)?\\b").matcher(html).find();
        if (usesVue && vue < 0) {
            problems.add("the page uses Vue but loads no Vue script; add <script src=\"${base}/element-plus/vue3.js\"></script>");
        }
        if (usesElement && element < 0) {
            problems.add("the page uses Element Plus but loads no Element Plus script; add <script src=\"${base}/element-plus/element.min.js\"></script> after the Vue script");
        }
        if (vue >= 0 && element >= 0 && element < vue) {
            problems.add("element.min.js is loaded before vue3.js; Element Plus needs the Vue global, load vue3.js first");
        }
        if (Pattern.compile("\\becharts\\.").matcher(html).find() && indexOfSrc(srcs, "echarts") < 0) {
            problems.add("the page uses echarts but loads no echarts script");
        }
        if (Pattern.compile("\\baxios\\.").matcher(html).find() && indexOfSrc(srcs, "axios") < 0) {
            problems.add("the page uses axios but loads no axios script (prefer the Erupt SDK for data access)");
        }
    }

    private static int indexOfSrc(List<String> srcs, String needle) {
        for (int i = 0; i < srcs.size(); i++) {
            String name = srcs.get(i).substring(srcs.get(i).lastIndexOf('/') + 1);
            if (name.contains(needle)) return i;
        }
        return -1;
    }

    /**
     * Bracket balance of a JavaScript source, skipping strings, template
     * literals (with nested {@code ${}} expressions), comments and regex
     * literals. Returns a description of the first problem, or null when the
     * brackets pair up and nothing is left unterminated.
     */
    static String bracketProblem(String js) {
        Deque<Character> stack = new ArrayDeque<>();
        // Template literal nesting: each entry is the bracket depth at which its ${ opened
        Deque<Integer> templates = new ArrayDeque<>();
        int i = 0, n = js.length();
        char prevSignificant = 0;
        String prevWord = "";
        while (i < n) {
            char c = js.charAt(i);
            if (c == '/' && i + 1 < n && js.charAt(i + 1) == '/') {
                i = skipLineComment(js, i);
            } else if (c == '/' && i + 1 < n && js.charAt(i + 1) == '*') {
                int end = js.indexOf("*/", i + 2);
                if (end < 0) return "has an unterminated /* comment";
                i = end + 2;
            } else if (c == '\'' || c == '"') {
                int end = skipString(js, i, c);
                if (end < 0) return "has an unterminated string literal";
                i = end;
                prevSignificant = c;
                prevWord = "";
            } else if (c == '`') {
                int[] scan = scanTemplate(js, i + 1);
                if (scan[0] < 0) return "has an unterminated template literal";
                if (scan[0] == 1) {
                    // Hit ${ : the expression is code until the matching brace closes
                    templates.push(stack.size());
                    stack.push('{');
                }
                i = scan[1];
                prevSignificant = '`';
                prevWord = "";
            } else if (c == '/' && startsRegex(prevSignificant, prevWord)) {
                int end = skipRegex(js, i);
                if (end < 0) return "has an unterminated regular expression";
                i = end;
                prevSignificant = '/';
                prevWord = "";
            } else if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
                i++;
                prevSignificant = c;
                prevWord = "";
            } else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) return "has an unexpected closing '" + c + "'";
                char open = stack.pop();
                if (open != matching(c)) return "has mismatched brackets: '" + open + "' closed by '" + c + "'";
                i++;
                // Closing brace of a ${ expression: resume scanning the template text
                if (c == '}' && !templates.isEmpty() && templates.peek() == stack.size()) {
                    templates.pop();
                    int[] scan = scanTemplate(js, i);
                    if (scan[0] < 0) return "has an unterminated template literal";
                    if (scan[0] == 1) {
                        templates.push(stack.size());
                        stack.push('{');
                    }
                    i = scan[1];
                    prevSignificant = '`';
                } else {
                    prevSignificant = c;
                }
                prevWord = "";
            } else if (Character.isLetterOrDigit(c) || c == '_' || c == '$') {
                int start = i;
                while (i < n && (Character.isLetterOrDigit(js.charAt(i)) || js.charAt(i) == '_' || js.charAt(i) == '$')) i++;
                prevWord = js.substring(start, i);
                prevSignificant = 'a';
            } else {
                if (!Character.isWhitespace(c)) {
                    prevSignificant = c;
                    prevWord = "";
                }
                i++;
            }
        }
        if (!stack.isEmpty()) return "has an unclosed '" + stack.peek() + "'";
        return null;
    }

    private static char matching(char close) {
        return switch (close) {
            case ')' -> '(';
            case ']' -> '[';
            default -> '{';
        };
    }

    private static int skipLineComment(String js, int i) {
        int end = js.indexOf('\n', i);
        return end < 0 ? js.length() : end + 1;
    }

    // Index after the closing quote; -1 when unterminated (a raw newline ends a
    // plain string too, which is exactly the truncation symptom we want)
    private static int skipString(String js, int i, char quote) {
        for (int j = i + 1; j < js.length(); j++) {
            char c = js.charAt(j);
            if (c == '\\') j++;
            else if (c == quote) return j + 1;
            else if (c == '\n') return -1;
        }
        return -1;
    }

    // Scan template text from i. Returns {0, indexAfterClosingBacktick}, {1, indexAfterDollarBrace}
    // when a ${ expression opens, or {-1, 0} when the literal is unterminated
    private static int[] scanTemplate(String js, int i) {
        for (int j = i; j < js.length(); j++) {
            char c = js.charAt(j);
            if (c == '\\') j++;
            else if (c == '`') return new int[]{0, j + 1};
            else if (c == '$' && j + 1 < js.length() && js.charAt(j + 1) == '{') return new int[]{1, j + 2};
        }
        return new int[]{-1, 0};
    }

    private static int skipRegex(String js, int i) {
        boolean inClass = false;
        for (int j = i + 1; j < js.length(); j++) {
            char c = js.charAt(j);
            if (c == '\\') j++;
            else if (c == '\n') return -1;
            else if (c == '[') inClass = true;
            else if (c == ']') inClass = false;
            else if (c == '/' && !inClass) {
                j++;
                while (j < js.length() && Character.isLetter(js.charAt(j))) j++;
                return j;
            }
        }
        return -1;
    }

    // A '/' is a regex when it cannot be a division: at the start, after an
    // operator or opening bracket, or after keywords such as return / typeof
    private static boolean startsRegex(char prevSignificant, String prevWord) {
        if (!prevWord.isEmpty()) return REGEX_PRECEDING_KEYWORDS.contains(prevWord);
        if (prevSignificant == 0) return true;
        return "(,=:[!&|?{};+-*%<>~^".indexOf(prevSignificant) >= 0;
    }

}
