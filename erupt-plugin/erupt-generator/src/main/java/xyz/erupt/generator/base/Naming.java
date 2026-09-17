package xyz.erupt.generator.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Naming conversions between database identifiers and java identifiers.
 *
 * @author YuePeng
 * date 2026-09-17
 */
public class Naming {

    private static final String ERUPT_PREFIX = "e_";

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "record", "var"));

    //column names are free to collide with the java grammar, field names are not
    public static String fieldName(String column) {
        String name = lineToHump(column);
        if (name.isEmpty()) return column;
        if (Character.isDigit(name.charAt(0))) name = "_" + name;
        return KEYWORDS.contains(name) ? name + "Value" : name;
    }

    //userName -> user_name
    public static String humpToLine(String str) {
        String hump = str.replaceAll("[A-Z]", "_$0").toLowerCase(Locale.ROOT);
        return hump.startsWith("_") ? hump.substring(1) : hump;
    }

    //user_name -> userName, USER_NAME -> userName
    public static String lineToHump(String str) {
        //upper case only identifiers (oracle, db2...) would otherwise become USERNAME
        boolean lower = str.equals(str.toUpperCase(Locale.ROOT));
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : str.toCharArray()) {
            if ('_' == c || ' ' == c || '-' == c) {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(lower ? Character.toLowerCase(c) : c);
            }
        }
        return sb.toString();
    }

    //e_user_name -> UserName, the erupt tables are the only ones named by a convention we know
    public static String className(String table) {
        String name = table.regionMatches(true, 0, ERUPT_PREFIX, 0, ERUPT_PREFIX.length())
                ? table.substring(ERUPT_PREFIX.length()) : table;
        name = lineToHump(name);
        if (name.isEmpty()) name = lineToHump(table);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    //user_name -> User Name, used when the column carries no comment
    public static String title(String column) {
        StringBuilder sb = new StringBuilder();
        for (String word : humpToLine(column).split("_")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.length() == 0 ? column : sb.toString();
    }

}
