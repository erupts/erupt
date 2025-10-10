package xyz.erupt.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

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

    private static final Pattern XSS_PATTERN = Pattern.compile(
            // Script 标签
            "<script\\b[^>]*>(.*?)</script>|" +
            // 主要事件处理器
            "\\b(?:onclick|onload|onmouseover|onfocus|onerror|onchange|onsubmit|onkeydown|onkeyup|onblur|onresize|onscroll|ondblclick|onmousedown|onmouseup|onmousemove|onmouseout|oninput|onpaste|oncut|oncopy|ondrag|ondrop|onreset|onselect|onwheel)\\s*\\s*=" +
            // 伪协议
            "\\b(?:javascript|vbscript|data):|" +
            // 危险的属性值
            "\\b(?:src|href|action)\\s*\\s*=\\s*(['\"]?)\\s*(?:javascript|vbscript|data):|" +
            // 危险的函数调用
            "\\b(?:eval|expression|setTimeout|setInterval|Function|alert|confirm|prompt)\\s*\\s*\\(|" +
            // 危险的HTML标签（只检测明显危险的标签）
            "<(?:iframe|object|embed|applet)\\b[^>]*>|" +
            // 样式中的表达式
            "\\bexpression\\s*\\s*\\(|" +
            // 危险的DOM操作
            "\\b(?:innerHTML|outerHTML|document\\.write|document\\.writeln)\\s*\\s*=",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );

    /**
     * true = Detecting suspicious XSS fragments
     */
    public static boolean xssInspect(String value) {
        return value != null && XSS_PATTERN.matcher(value).find();
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
