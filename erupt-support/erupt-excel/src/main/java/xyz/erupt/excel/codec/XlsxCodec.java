package xyz.erupt.excel.codec;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.excel.util.ExcelUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel through POI: the historical default. Exports carry a "condition" sheet listing the query,
 * templates carry cell validation and dropdowns for the form's options, and {@code .xls} is read as
 * well as {@code .xlsx}. The {@code DataProxy.excelExport / excelImport} hooks receive the workbook here.
 */
@Component
public class XlsxCodec implements TableCodec {

    public static final String FORMAT = "xlsx";

    private static final String SIMPLE_CELL_ERR = "Please select or enter a valid option, or download the latest template and try again!";

    @Override
    public String format() {
        return FORMAT;
    }

    @Override
    public String name() {
        return "Excel";
    }

    @Override
    public String mediaType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    // both container formats are read; the first bytes tell them apart
    public boolean accept(String fileName) {
        if (null == fileName) return false;
        String name = fileName.toLowerCase();
        return name.endsWith(".xlsx") || name.endsWith(".xls");
    }

    @Override
    public void write(TableSheet sheet, OutputStream out) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            if (sheet.template()) {
                this.template(wb, sheet);
            } else {
                this.export(wb, sheet);
                DataProxyInvoke.invoke(sheet.eruptModel(), (dataProxy -> dataProxy.excelExport(wb)));
                this.conditionSheet(wb, sheet.conditions());
            }
            wb.write(out);
        }
    }

    // Import, Excel only: rows of header title → cell text for the service to map to records
    public List<Map<String, String>> read(EruptModel eruptModel, InputStream in) throws IOException {
        // the first bytes tell the two container formats apart; the file name is not needed
        in = in.markSupported() ? in : new java.io.BufferedInputStream(in);
        in.mark(8);
        int first = in.read();
        in.reset();
        try (Workbook wb = first == 0xD0 ? new HSSFWorkbook(in) : new XSSFWorkbook(in)) {
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.excelImport(wb)));
            return this.rows(eruptModel, wb);
        }
    }

    private void export(Workbook wb, TableSheet table) {
        Sheet sheet = wb.createSheet(table.title());
        sheet.setZoom(160);
        sheet.createFreezePane(0, 1, 1, 1);
        Row head = sheet.createRow(0);
        CellStyle headStyle = ExcelUtil.beautifyExcelStyle(wb);
        Font headFont = wb.createFont();
        headFont.setColor(IndexedColors.WHITE.index);
        headFont.setBold(true);
        headStyle.setFont(headFont);
        headStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.index);
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        List<TableSheet.TableColumn> columns = table.columns();
        for (int c = 0; c < columns.size(); c++) {
            TableSheet.TableColumn column = columns.get(c);
            View view = column.view();
            boolean wide = null != view && view.type() == ViewType.DATE_TIME;
            sheet.setColumnWidth(c, (column.title().length() + (wide ? 12 : 10)) * 256);
            Cell cell = head.createCell(c);
            cell.setCellStyle(headStyle);
            cell.setCellValue(column.title());
        }
        CellStyle style = ExcelUtil.beautifyExcelStyle(wb);
        CellStyle dateStyle = ExcelUtil.beautifyExcelStyle(wb);
        dateStyle.setDataFormat((short) 14);
        CellStyle dateTimeStyle = ExcelUtil.beautifyExcelStyle(wb);
        dateTimeStyle.setDataFormat((short) 22);
        int rowIndex = 0;
        for (List<Object> values : table.rows()) {
            Row row = sheet.createRow(++rowIndex);
            for (int c = 0; c < columns.size(); c++) {
                Cell cell = row.createCell(c);
                cell.setCellStyle(style);
                Object value = values.get(c);
                if (null == value) continue;
                if (value instanceof LocalDate date) {
                    cell.setCellStyle(dateStyle);
                    cell.setCellValue(date);
                } else if (value instanceof LocalDateTime date) {
                    cell.setCellStyle(dateTimeStyle);
                    cell.setCellValue(date);
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        }
    }

    private void conditionSheet(Workbook wb, List<String[]> conditions) {
        Sheet sheet = wb.createSheet("condition");
        sheet.createFreezePane(0, 1, 1, 1);
        sheet.setColumnWidth(0, 16 * 256);
        sheet.setColumnWidth(1, 12 * 256);
        sheet.setColumnWidth(2, 50 * 256);
        Row head = sheet.createRow(0);
        head.createCell(0).setCellValue("name");
        head.createCell(1).setCellValue("expr");
        head.createCell(2).setCellValue("value");
        if (null == conditions) return;
        for (String[] condition : conditions) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            for (int i = 0; i < condition.length; i++) row.createCell(i).setCellValue(condition[i]);
        }
    }

    // The template mirrors the form: required titles in red, references highlighted, options as dropdowns
    private void template(Workbook wb, TableSheet table) {
        EruptModel eruptModel = table.eruptModel();
        Sheet sheet = wb.createSheet(table.title());
        sheet.setZoom(160);
        sheet.createFreezePane(0, 1, 1, 1);
        Row headRow = sheet.createRow(0);
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        List<TableSheet.TableColumn> columns = table.columns();
        for (int c = 0; c < columns.size(); c++) {
            TableSheet.TableColumn column = columns.get(c);
            Edit edit = column.edit();
            EruptFieldModel fieldModel = column.field();
            Cell cell = headRow.createCell(c);
            sheet.setColumnWidth(c, (column.title().length() + 10) * 256);
            switch (edit.type()) {
                case BOOLEAN -> sheet.addValidationData(this.validation(sheet, c, SIMPLE_CELL_ERR,
                        dvHelper.createExplicitListConstraint(new String[]{edit.boolType().trueText(), edit.boolType().falseText()})));
                case CHOICE -> {
                    List<VLModel> vls = EruptUtil.getChoiceList(eruptModel, fieldModel).stream().filter(it -> !it.isDisable()).toList();
                    if (!vls.isEmpty()) this.dropDown(fieldModel, sheet, vls, new CellRangeAddress(1, 1000, c, c));
                }
                case SLIDER -> sheet.addValidationData(this.validation(sheet, c,
                        "Select or enter a valid option, range: " + edit.sliderType().min() + " - " + edit.sliderType().max(),
                        dvHelper.createNumericConstraint(DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.OperatorType.BETWEEN,
                                Integer.toString(edit.sliderType().min()), Integer.toString(edit.sliderType().max()))));
                case DATE -> {
                    if (EruptUtil.isDateField(fieldModel.getFieldReturnName())) {
                        sheet.addValidationData(this.validation(sheet, c, "Please select or enter the valid time",
                                dvHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, "1900-01-01", "2999-12-31", "yyyy-MM-dd")));
                    }
                }
                default -> {
                }
            }
            CellStyle style = wb.createCellStyle();
            style.setLocked(true);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setAlignment(HorizontalAlignment.CENTER);
            Font font = wb.createFont();
            font.setBold(true);
            if (edit.notNull()) font.setColor(Font.COLOR_RED);
            if (edit.type() == EditType.REFERENCE_TREE || edit.type() == EditType.REFERENCE_TABLE) {
                style.setFillForegroundColor(IndexedColors.YELLOW1.index);
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            style.setFont(font);
            cell.setCellStyle(style);
            cell.setCellValue(column.title());
        }
    }

    private DataValidation validation(Sheet sheet, int colIndex, String errHint, DataValidationConstraint constraint) {
        CellRangeAddressList regions = new CellRangeAddressList(1, 1000, colIndex, colIndex);
        DataValidation validation = sheet.getDataValidationHelper().createValidation(constraint, regions);
        validation.createErrorBox("error", errHint);
        return validation;
    }

    private void dropDown(EruptFieldModel fieldModel, Sheet targetSheet, List<VLModel> options, CellRangeAddress targetRegion) {
        Workbook workbook = targetSheet.getWorkbook();
        Sheet dictSheet = workbook.createSheet(fieldModel.getFieldName());
        workbook.setSheetHidden(workbook.getSheetIndex(dictSheet), true);
        for (int i = 0; i < options.size(); i++) {
            dictSheet.createRow(i).createCell(0).setCellValue(options.get(i).getLabel());
        }
        Name name = workbook.createName();
        name.setNameName(fieldModel.getFieldName() + "Range_" + System.nanoTime());
        name.setRefersToFormula(dictSheet.getSheetName() + "!$A$1:$A$" + options.size());
        DataValidationHelper helper = targetSheet.getDataValidationHelper();
        DataValidation validation = helper.createValidation(helper.createFormulaListConstraint(name.getNameName()), new CellRangeAddressList(
                targetRegion.getFirstRow(), targetRegion.getLastRow(), targetRegion.getFirstColumn(), targetRegion.getLastColumn()));
        validation.setShowErrorBox(true);
        // XSSF semantics are inverted from HSSF: true shows the dropdown arrow
        validation.setSuppressDropDownArrow(true);
        targetSheet.addValidationData(validation);
    }

    private List<Map<String, String>> rows(EruptModel eruptModel, Workbook wb) {
        Sheet sheet = wb.getSheetAt(0);
        Row titleRow = sheet.getRow(0);
        List<Map<String, String>> rows = new ArrayList<>();
        if (null == titleRow) return rows;
        Map<String, EruptFieldModel> byTitle = new HashMap<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            byTitle.put(fieldModel.getEruptField().edit().title(), fieldModel);
        }
        int cells = titleRow.getPhysicalNumberOfCells();
        String[] titles = new String[cells];
        boolean[] dates = new boolean[cells];
        for (int i = 0; i < cells; i++) {
            titles[i] = titleRow.getCell(i).getStringCellValue();
            EruptFieldModel fieldModel = byTitle.get(titles[i]);
            dates[i] = null != fieldModel && fieldModel.getEruptField().edit().type() == EditType.DATE;
        }
        DataFormatter formatter = new DataFormatter();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (null == row || row.getPhysicalNumberOfCells() == 0) continue;
            Map<String, String> values = new LinkedHashMap<>();
            for (int c = 0; c < cells; c++) {
                Cell cell = row.getCell(c);
                if (null == cell || CellType.BLANK == cell.getCellType()) continue;
                String text;
                if (dates[c] && cell.getCellType() == CellType.NUMERIC) {
                    // a real date cell comes back as ISO text, the same thing a text format would carry
                    Date date = cell.getDateCellValue();
                    text = DateUtil.getFormatDate(date, DateUtil.ISO_8601);
                } else {
                    text = switch (cell.getCellType()) {
                        case NUMERIC -> formatter.formatCellValue(cell);
                        case STRING -> cell.getStringCellValue();
                        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                        case FORMULA -> cell.getCellFormula();
                        case ERROR -> String.valueOf(cell.getErrorCellValue());
                        default -> "";
                    };
                }
                if (!text.isEmpty()) values.put(titles[c], text);
            }
            if (!values.isEmpty()) rows.add(values);
        }
        return rows;
    }

    static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}
