package xyz.erupt.excel.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ExcelUtil {

    public static CellStyle beautifyExcelStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.index);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.index);
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.index);
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.index);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    public static OutputStream downLoadFile(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            String headStr = "attachment; filename=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            response.setContentType("application/x-download");
            response.setHeader(CONTENT_DISPOSITION_HEADER, headStr);
            response.addHeader("Access-Control-Expose-Headers", CONTENT_DISPOSITION_HEADER);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return response.getOutputStream();
        } catch (IOException e) {
            log.error("erupt downLoad error", e);
            return null;
        }
    }
}
