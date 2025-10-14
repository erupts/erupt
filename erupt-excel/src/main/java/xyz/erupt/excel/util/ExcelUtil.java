package xyz.erupt.excel.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpHeaders;

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

    public static OutputStream downLoadFile(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            String headStr = "attachment; filename=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/x-download");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, headStr);
            response.addHeader("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return response.getOutputStream();
        } catch (IOException e) {
            log.error("erupt downLoad error", e);
            return null;
        }
    }
}
