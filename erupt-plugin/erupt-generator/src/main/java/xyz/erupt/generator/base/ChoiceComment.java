package xyz.erupt.generator.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Legacy schemas document their code columns in the comment rather than in a
 * dictionary table: "status 0-disabled 1-enabled". Those pairs are the choice list.
 *
 * @author YuePeng
 * date 2026-09-17
 */
public class ChoiceComment {

    //value on the left of a separator, label on the right, both kept short
    private static final Pattern PAIR = Pattern.compile("([0-9A-Za-z_]{1,12})\\s*[-=:：]\\s*([^\\s,;、)，；）]{1,20})");

    //punctuation a comment uses to separate the name of the column from its dictionary
    private static final String SEPARATORS = ":：(（[【,，-";

    //one pair is a sentence, several are a convention
    private static final int MIN_PAIRS = 2;

    /**
     * @return what the comment says about a code column, null when it says nothing
     */
    public static Choice parse(String comment) {
        if (null == comment) return null;
        Matcher matcher = PAIR.matcher(comment);
        Map<String, String> vl = new LinkedHashMap<>();
        boolean worded = false;
        int head = comment.length();
        while (matcher.find()) {
            //a label made only of digits comes from a date or a range, not from a dictionary
            if (!matcher.group(2).matches("[0-9]+")) worded = true;
            if (vl.isEmpty()) head = matcher.start();
            vl.putIfAbsent(matcher.group(1), matcher.group(2));
        }
        if (!worded || vl.size() < MIN_PAIRS) return null;
        String code = "choiceType = @ChoiceType(vl = {" + vl.entrySet().stream()
                .map(it -> "@VL(value = \"" + escape(it.getKey()) + "\", label = \"" + escape(it.getValue()) + "\")")
                .collect(Collectors.joining(", ")) + "})";
        //what stands in front of the dictionary is the name of the column
        return new Choice(strip(comment.substring(0, head)), code);
    }

    private static String strip(String title) {
        String text = title.trim();
        while (!text.isEmpty() && SEPARATORS.indexOf(text.charAt(text.length() - 1)) >= 0) {
            text = text.substring(0, text.length() - 1).trim();
        }
        return text;
    }

    public record Choice(String title, String code) {
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private ChoiceComment() {
    }

}
