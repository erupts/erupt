package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.*;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author YuePeng
 * date 12/4/18.
 */
@Service
public class EruptExcelService {

    public static final String XLS_FORMAT = ".xls";

    public static final String XLSX_FORMAT = ".xlsx";

    private static final String SIMPLE_CELL_ERR = "请选择或输入有效的选项，或下载最新模版重试！";

    /**
     * excel导出，展示的格式和view表格一致
     *
     * @return Workbook
     */
    public Workbook exportExcel(EruptModel eruptModel, Page page) {
//        XSSFWorkbook
//        SXSSFWorkbook
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(eruptModel.getErupt().name());
        sheet.setZoom(160);
        //冻结首行
        sheet.createFreezePane(0, 1, 1, 1);
        int rowIndex = 0;
        int colNum = 0;
        Row row = sheet.createRow(rowIndex);
        CellStyle headStyle = ExcelUtil.beautifyExcelStyle(wb);
        Font headFont = wb.createFont();
        headFont.setColor(IndexedColors.WHITE.index);
        headStyle.setFont(headFont);
        headStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.index);
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headFont.setBold(true);
        headStyle.setFont(headFont);
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            for (View view : fieldModel.getEruptField().views()) {
                if (view.show() && view.export()) {
                    Cell cell = row.createCell(colNum);
                    cell.setCellStyle(headStyle);
                    cell.setCellValue(view.title());
//                    if (StringUtils.isNotBlank(view.width())) {
//                        sheet.setColumnWidth(colNum, view.width());
//                    }
                    colNum++;
                }
            }
        }
        CellStyle style = ExcelUtil.beautifyExcelStyle(wb);
        for (Map<String, Object> map : page.getList()) {
            int dataColNum = 0;
            row = sheet.createRow(++rowIndex);
            for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
                for (View view : fieldModel.getEruptField().views()) {
                    if (view.show() && view.export()) {
                        Cell cell = row.createCell(dataColNum);
                        cell.setCellStyle(style);
                        Object val;
                        if (StringUtils.isNotBlank(view.column())) {
                            val = map.get(fieldModel.getFieldName() + "_" + view.column());
                        } else {
                            val = map.get(fieldModel.getFieldName());
                        }
                        if (null != val) {
                            cell.setCellValue(val.toString());
                        }
                        dataColNum++;
                    }
                }
            }
        }
        return wb;
    }

    public List<JsonObject> excelToEruptObject(EruptModel eruptModel, Workbook workbook) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        Row titleRow = sheet.getRow(0);
        Map<String, EruptFieldModel> editTitleMappingEruptField = new HashMap<>(eruptModel.getEruptFieldModels().size());
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            editTitleMappingEruptField.put(fieldModel.getEruptField().edit().title(), fieldModel);
        }
        Map<Integer, EruptFieldModel> cellIndexMapping = new HashMap<>(titleRow.getPhysicalNumberOfCells());
        Map<Integer, Map<String, Object>> cellIndexJoinEruptMap = new HashMap<>();
        for (int i = 0; i < titleRow.getPhysicalNumberOfCells(); i++) {
            String titleName = titleRow.getCell(i).getStringCellValue();
            EruptFieldModel eruptFieldModel = editTitleMappingEruptField.get(titleName);
            cellIndexMapping.put(i, eruptFieldModel);
            Edit edit = eruptFieldModel.getEruptField().edit();
            switch (edit.type()) {
                case CHOICE:
                    Map<String, String> map = EruptUtil.getChoiceMap(edit.choiceType());
                    Map<String, Object> choiceMap = new HashMap<>(map.size());
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        choiceMap.put(entry.getValue(), entry.getKey());
                    }
                    cellIndexJoinEruptMap.put(i, choiceMap);
                    break;
                case BOOLEAN:
                    Map<String, Object> boolMap = new HashMap<>(2);
                    BoolType boolType = eruptFieldModel.getEruptField().edit().boolType();
                    boolMap.put(boolType.trueText(), true);
                    boolMap.put(boolType.falseText(), false);
                    cellIndexJoinEruptMap.put(i, boolMap);
                    break;
                case REFERENCE_TREE:
                    IEruptDataService iEruptDataService = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz());
                    List<Column> columns = new ArrayList<>();
                    columns.add(new Column(edit.referenceTreeType().id(), edit.referenceTreeType().id()));
                    columns.add(new Column(edit.referenceTreeType().label(), edit.referenceTreeType().label()));
                    Collection<Map<String, Object>> list = iEruptDataService.queryColumn(EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName()), columns, EruptQuery.builder().build());
                    Map<String, Object> refTreeMap = new HashMap<>(list.size());
                    for (Map<String, Object> m : list) {
                        Object label = m.get(edit.referenceTreeType().label());
                        if (null == label) {
                            continue;
                        }
                        refTreeMap.put(label.toString(), m.get(edit.referenceTreeType().id()));
                    }
                    cellIndexJoinEruptMap.put(i, refTreeMap);
                    break;
                case REFERENCE_TABLE:
                    IEruptDataService eruptDataProcessor = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz());
                    List<Column> columnList = new ArrayList<>();
                    columnList.add(new Column(edit.referenceTableType().id(), edit.referenceTableType().id()));
                    columnList.add(new Column(edit.referenceTableType().label(), edit.referenceTableType().label()));
                    Collection<Map<String, Object>> list2 = eruptDataProcessor.queryColumn(EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName()), columnList, EruptQuery.builder().build());
                    Map<String, Object> refTreeMap2 = new HashMap<>(list2.size());
                    for (Map<String, Object> m : list2) {
                        Object label = m.get(edit.referenceTableType().label());
                        if (null == label) {
                            continue;
                        }
                        refTreeMap2.put(label.toString(), m.get(edit.referenceTableType().id()));
                    }
                    cellIndexJoinEruptMap.put(i, refTreeMap2);
                    break;
                default:
                    break;
            }
        }
        List<JsonObject> listObject = new ArrayList<>();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row.getPhysicalNumberOfCells() == 0) {
                continue;
            }
            JsonObject jsonObject = new JsonObject();
            for (int cellNum = 0; cellNum < titleRow.getPhysicalNumberOfCells(); cellNum++) {
                Cell cell = row.getCell(cellNum);
                EruptFieldModel eruptFieldModel = cellIndexMapping.get(cellNum);
                if (null != cell && CellType.BLANK != cell.getCellTypeEnum()) {
                    Edit edit = eruptFieldModel.getEruptField().edit();
                    switch (edit.type()) {
                        case REFERENCE_TABLE:
                        case REFERENCE_TREE:
                            JsonObject jo = new JsonObject();
                            try {
                                if (edit.type() == EditType.REFERENCE_TREE) {
                                    jo.addProperty(edit.referenceTreeType().id(),
                                            cellIndexJoinEruptMap.get(cellNum).get(cell.getStringCellValue()).toString());
                                } else if (edit.type() == EditType.REFERENCE_TABLE) {
                                    jo.addProperty(edit.referenceTableType().id(),
                                            cellIndexJoinEruptMap.get(cellNum).get(cell.getStringCellValue()).toString());
                                }
                            } catch (Exception e) {
                                throw new Exception(edit.title() + " -> " + this.getStringCellValue(cell) + "数据不存在");
                            }
                            jsonObject.add(eruptFieldModel.getFieldName(), jo);
                            break;
                        case CHOICE:
                            try {
                                jsonObject.addProperty(eruptFieldModel.getFieldName(), cellIndexJoinEruptMap.get(cellNum)
                                        .get(cell.getStringCellValue()).toString());
                            } catch (Exception e) {
                                throw new Exception(edit.title() + " -> " + this.getStringCellValue(cell) + "数据不存在");
                            }
                            break;
                        case BOOLEAN:
                            Boolean bool = (Boolean) cellIndexJoinEruptMap.get(cellNum).get(cell.getStringCellValue());
                            jsonObject.addProperty(eruptFieldModel.getFieldName(), bool);
                            break;
                        default:
                            String rn = eruptFieldModel.getFieldReturnName();
                            if (String.class.getSimpleName().equals(rn)) {
                                jsonObject.addProperty(eruptFieldModel.getFieldName(), this.getStringCellValue(cell));
                            } else if (JavaType.NUMBER.equals(rn)) {
                                jsonObject.addProperty(eruptFieldModel.getFieldName(), cell.getNumericCellValue());
                            } else if (Date.class.getSimpleName().equals(rn)) {
                                jsonObject.addProperty(eruptFieldModel.getFieldName(), DateUtil.getSimpleFormatDate(cell.getDateCellValue()));
                            }
                            break;
                    }
                }
            }
            listObject.add(jsonObject);
        }
        return listObject;
    }

    public String getStringCellValue(Cell cell) {
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue() + "";
    }


    //模板的格式和edit输入框一致
    public void createExcelTemplate(EruptModel eruptModel, HttpServletRequest request, HttpServletResponse response) {
        Workbook wb = new HSSFWorkbook();
        //基本信息
        Sheet sheet = wb.createSheet(eruptModel.getErupt().name());
        sheet.setZoom(160);
        //冻结首行
        sheet.createFreezePane(0, 1, 1, 1);
        Row headRow = sheet.createRow(0);
        int cellNum = 0;
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.show() && !edit.readonly().add() && StringUtils.isNotBlank(edit.title())
                    && AnnotationUtil.getEditTypeMapping(edit.type()).excelOperator()) {
                Cell cell = headRow.createCell(cellNum);
                //256表格一个字节的宽度
                sheet.setColumnWidth(cellNum, (edit.title().length() + 10) * 256);
                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                switch (edit.type()) {
                    case BOOLEAN:
                        sheet.addValidationData(generateValidation(cellNum, SIMPLE_CELL_ERR, DVConstraint.createExplicitListConstraint(new String[]{edit.boolType().trueText(),
                                edit.boolType().falseText()})));
                        break;
                    case CHOICE:
                        List<VLModel> vls = EruptUtil.getChoiceList(fieldModel.getEruptField().edit().choiceType());
                        String[] arr = new String[vls.size()];
                        long length = 0;
                        for (int i = 0; i < vls.size(); i++) {
                            arr[i] = vls.get(i).getLabel();
                            length += arr[i].length();
                        }
                        //下拉框不允许超过255字节
                        if (length <= 255) {
                            sheet.addValidationData(generateValidation(cellNum, SIMPLE_CELL_ERR, DVConstraint.createExplicitListConstraint(arr)));
                        }
                        break;
                    case SLIDER:
                        sheet.addValidationData(generateValidation(cellNum,
                                "请选择或输入有效的选项，区间：" + edit.sliderType().min() + " - " + edit.sliderType().max(), dvHelper.createNumericConstraint(
                                        DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.OperatorType.BETWEEN,
                                        Integer.toString(edit.sliderType().min()), Integer.toString(edit.sliderType().max()))));
                        break;
                    case DATE:
                        if (fieldModel.getFieldReturnName().equals(Date.class.getSimpleName())) {
                            sheet.addValidationData(generateValidation(cellNum, "请选择或输入有效时间！", dvHelper.createDateConstraint(DVConstraint.OperatorType.BETWEEN,
                                    "1900-01-01", "2999-12-31", "yyyy-MM-dd")));
                        }
                        break;
                    default:
                        break;
                }
                //单元格格式
                CellStyle style = wb.createCellStyle();
                style.setLocked(true);
                Font font = wb.createFont();
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                style.setAlignment(HorizontalAlignment.CENTER);
                font.setBold(true);
                if (edit.notNull()) {
                    font.setColor(Font.COLOR_RED);
                }
                if (edit.type() == EditType.REFERENCE_TREE || edit.type() == EditType.REFERENCE_TABLE) {
                    style.setFillForegroundColor(IndexedColors.YELLOW1.index);
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
                style.setFont(font);
                cell.setCellStyle(style);
                //值
                cell.setCellValue(fieldModel.getEruptField().edit().title());
                cellNum++;
            }
        }
        try {
            wb.write(HttpUtil.downLoadFile(request, response, eruptModel.getErupt().name() + "_导入模板" + XLS_FORMAT));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private DataValidation generateValidation(int colIndex, String errHint, DataValidationConstraint constraint) {
        // 设置数据有效性加载在哪个单元格上。
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(1, 1000, colIndex, colIndex);
        DataValidation dataValidationList = new HSSFDataValidation(regions, constraint);
        dataValidationList.createErrorBox("错误", errHint);
        return dataValidationList;
    }

}
