package xyz.erupt.core.util;

import org.apache.poi.ss.usermodel.*;

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
}
