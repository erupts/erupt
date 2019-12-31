package xyz.erupt.core.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author liyuepeng
 * @date 12/4/18.
 */
public class HttpUtil {

    public static OutputStream downLoadFile(HttpServletResponse response, String fileName) {
        try {
            String headStr = "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", headStr);
            response.setCharacterEncoding("utf-8");
            return response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
