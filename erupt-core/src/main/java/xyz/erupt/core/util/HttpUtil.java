package xyz.erupt.core.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author YuePeng
 * date 12/4/18.
 */
public class HttpUtil {

    public static OutputStream downLoadFile(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            String headStr = "attachment; filename=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", headStr);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
