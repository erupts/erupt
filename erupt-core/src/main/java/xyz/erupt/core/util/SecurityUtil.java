package xyz.erupt.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
            // Script Tag
            "<script\\b[^>]*>(.*?)</script>|" +
            // event handler
            "\\b(?:onclick|onload|onmouseover|onfocus|onerror|onchange|onsubmit|onkeydown|onkeyup|onblur|onresize|onscroll|ondblclick|onmousedown|onmouseup|onmousemove|onmouseout|oninput|onpaste|oncut|oncopy|ondrag|ondrop|onreset|onselect|onwheel)\\s*\\s*=" +
            // Fake agreement
            "\\b(?:javascript|vbscript|data):|" +
            // Dangerous attribute value
            "\\b(?:src|href|action)\\s*\\s*=\\s*(['\"]?)\\s*(?:javascript|vbscript|data):|" +
            // Dangerous function call
            "\\b(?:eval|expression|setTimeout|setInterval|Function|alert|confirm|prompt)\\s*\\s*\\(|" +
            // Dangerous HTML tags (only detecting obvious dangerous tags)
            "<(?:iframe|object|embed|applet)\\b[^>]*>|" +
            // Expressions in the style
            "\\bexpression\\s*\\s*\\(|" +
            // Dangerous DOM operations
            "\\b(?:innerHTML|outerHTML|document\\.write|document\\.writeln)\\s*\\s*=",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );

    /**
     * true = Detecting suspicious XSS fragments
     */
    public static boolean xssInspect(String value) {
        return value != null && XSS_PATTERN.matcher(value).find();
    }

    // Detection of Cross-Site Request Forgery
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
