package xyz.erupt.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author YuePeng
 * date 2019-10-30.
 */
@Slf4j
public class SecurityUtil {

    // xss跨站脚本检测
    public static boolean xssInspect(String value) {
        if (StringUtils.isNotBlank(value)) {
            // 避免script 标签
            Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免src形式的表达式
            scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 删除单个的<script ...> 标签
            scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 eval(...) 形式表达式
            scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 expression(...) 表达式
            scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 javascript: 表达式
            scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 vbscript: 表达式
            scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 onload= 表达式
            scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 onmouseover= 表达式
            scriptPattern = Pattern.compile("onmouseover(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 onfocus= 表达式
            scriptPattern = Pattern.compile("onfocus(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            if (scriptPattern.matcher(value).find()) {
                return true;
            }
            // 避免 onerror= 表达式
            scriptPattern = Pattern.compile("^onerror(.*?)=$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            return scriptPattern.matcher(value).find();
        }
        return false;
    }

    //检测 跨站请求伪造
    public static boolean csrfInspect(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (null != origin && !origin.contains(request.getHeader("Host"))) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain; charset=utf-8");
            try (PrintWriter out = response.getWriter()) {
                String text = "Illegal cross-site request!";
                out.append(text);
                throw new EruptWebApiRuntimeException(text);
            } catch (IOException e) {
                log.error("csrf inspect error", e);
            }
            return true;
        }
        return false;
    }
}
