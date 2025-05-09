package xyz.erupt.ai.util;

/**
 * @author YuePeng
 * date 2025/5/7 21:54
 */
public class MarkDownUtil {

    public static final String CODE_BLOCK_MARK = "```";

    public static String extractCodeBlock(String markdown) {
        if (markdown.startsWith(CODE_BLOCK_MARK) && markdown.endsWith(CODE_BLOCK_MARK)) {
            markdown = markdown.substring(CODE_BLOCK_MARK.length(), markdown.length() - CODE_BLOCK_MARK.length());
            // 处理可能存在的语言标识符，例如 ```java
            int firstNewlineIndex = markdown.indexOf('\n');
            if (firstNewlineIndex != -1) {
                markdown = markdown.substring(firstNewlineIndex + 1);
            }
        }
        return markdown;
    }

}
