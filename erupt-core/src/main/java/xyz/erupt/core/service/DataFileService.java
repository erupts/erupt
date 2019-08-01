package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.bean.Page;
import xyz.erupt.core.bean.TreeModel;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.HttpUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by liyuepeng on 12/4/18.
 */
@Service
public class DataFileService {

    public static final String XLS_FORMAT = ".xls";

    public static final String XLSX_FORMAT = ".xlsx";

    @Autowired
    private EruptDataController eruptDataController;

    public static final String SIMPLE_CELL_ERR = "请选择或输入有效的选项，或下载最新模版重试！";

    public static Object getCellValue(Cell cell) {
        Object cellValue;
        switch (cell.getCellTypeEnum()) {
            case NUMERIC: //数字
                cellValue = cell.getNumericCellValue();
                break;
            case STRING: //字符串
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN: //Boolean
                cellValue = cell.getBooleanCellValue();
                break;
            case FORMULA: //公式
                cellValue = cell.getCellFormula();
                break;
            case BLANK: //空值
                cellValue = null;
                break;
            case ERROR: //故障
                cellValue = null;
                break;
            default:
                cellValue = cell.getStringCellValue();
                break;
        }
        return cellValue;
    }

    /**
     * excel导出
     * 展示的格式和view表格一致
     *
     * @param eruptModel
     * @param Page
     * @param response
     */
    public Workbook exportExcel(EruptModel eruptModel, Page Page) {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(eruptModel.getErupt().name());
        //冻结首行
        sheet.createFreezePane(0, 1, 1, 1);
        int rowIndex = 0;
        int colNum = 0;
        Row row = sheet.createRow(rowIndex);
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            for (View view : fieldModel.getEruptField().views()) {
                if (view.show() && view.export()) {
                    Cell cell = row.createCell(colNum);
                    CellStyle style = wb.createCellStyle();
                    Font font = wb.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                    cell.setCellValue(view.title());
                    colNum++;
                }
            }
        }
        for (Map<String, Object> map : Page.getList()) {
            int dataColNum = 0;
            row = sheet.createRow(++rowIndex);
            for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
                for (View view : fieldModel.getEruptField().views()) {
                    if (view.show() && view.export()) {
                        Cell cell = row.createCell(dataColNum);
                        Object val = null;
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

    public List<JsonObject> excelToEruptObject(EruptModel eruptModel, Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        Row titleRow = sheet.getRow(0);
        Map<Integer, EruptFieldModel> cellIndexMapping = new HashMap<>();
        Map<Integer, Map<String, Object>> cellIndexJoinEruptMap = new HashMap<>();
        for (int i = 0; i < titleRow.getPhysicalNumberOfCells(); i++) {
            String comment = titleRow.getCell(i).getCellComment().getString().getString();
            EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(comment);
            cellIndexMapping.put(i, eruptFieldModel);
            switch (eruptFieldModel.getEruptField().edit().type()) {
                case CHOICE:
                    Map<String, Object> choiceMap = new HashMap<>();
                    for (VL vl : eruptFieldModel.getEruptField().edit().choiceType().vl()) {
                        choiceMap.put(vl.label(), vl.value());
                    }
                    cellIndexJoinEruptMap.put(i, choiceMap);
                    break;
                case BOOLEAN:
                    Map<String, Object> boolMap = new HashMap<>();
                    BoolType boolType = eruptFieldModel.getEruptField().edit().boolType();
                    boolMap.put(boolType.trueText(), true);
                    boolMap.put(boolType.trueText(), true);
                    cellIndexJoinEruptMap.put(i, boolMap);
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
            for (int cellNum = 0; cellNum < row.getPhysicalNumberOfCells(); cellNum++) {
                Cell cell = row.getCell(cellNum);
                EruptFieldModel eruptFieldModel = cellIndexMapping.get(cellNum);
                if (eruptFieldModel.getEruptField().edit().notNull() && CellType.BLANK == cell.getCellTypeEnum()) {
                    throw new RuntimeException(eruptFieldModel.getEruptField().edit().title() + "不允许为空");
                }
                if (CellType.BLANK != cell.getCellTypeEnum()) {
                    switch (eruptFieldModel.getEruptField().edit().type()) {
                        case REFERENCE_TREE:
                        case REFERENCE_TABLE:
                            JsonObject ref = new JsonObject();
                            //TODO
                            ref.addProperty(CoreService.getErupt(eruptFieldModel.getFieldReturnName()).getErupt().primaryKeyCol(), "");
                            jsonObject.add(eruptFieldModel.getFieldName(), ref);
                            break;
                        case CHOICE:
                            jsonObject.addProperty(eruptFieldModel.getFieldName(), cellIndexJoinEruptMap.get(cellNum)
                                    .get(cell.getStringCellValue()).toString());
                            break;
                        case BOOLEAN:
                            Boolean bool = (Boolean) cellIndexJoinEruptMap.get(cellNum).get(cell.getStringCellValue());
                            jsonObject.addProperty(eruptFieldModel.getFieldName(), bool);
                            break;
                        default:
                            switch (eruptFieldModel.getFieldReturnName()) {
                                case JavaType.STRING:
                                case JavaType.DATE:
                                    jsonObject.addProperty(eruptFieldModel.getFieldName(), cell.getStringCellValue());
                                    break;
                                case JavaType.NUMBER:
                                    jsonObject.addProperty(eruptFieldModel.getFieldName(), cell.getNumericCellValue());
                                    break;
                            }
                            break;
                    }
                }
            }
            System.out.println(jsonObject);
            listObject.add(jsonObject);
        }
        return listObject;
    }

    //模板的格式和edit输入框一致
    public void createExcelTemplate(EruptModel eruptModel, HttpServletResponse response) {
        Workbook wb = new HSSFWorkbook();
        //基本信息
        Sheet sheet = wb.createSheet(eruptModel.getErupt().name());
        //冻结首行
        sheet.createFreezePane(0, 1, 1, 1);
        //批注对象
        Drawing drawing = sheet.createDrawingPatriarch();
        Row hearRow = sheet.createRow(0);
        int cellNum = 0;
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.show() && StringUtils.isNotBlank(edit.title()) && AnnotationUtil.getEditTypeMapping(edit.type()).excelOperator()) {
                Cell cell = hearRow.createCell(cellNum);
                //256表格一个字节的宽度
                sheet.setColumnWidth(cellNum, (edit.title().length() + 10) * 256);
                DataValidationHelper dvHelper = sheet.getDataValidationHelper();

                switch (edit.type()) {
                    case BOOLEAN:
                        sheet.addValidationData(generateValidation(cellNum, SIMPLE_CELL_ERR, DVConstraint.createExplicitListConstraint(new String[]{edit.boolType().trueText(),
                                edit.boolType().falseText()})));
                        break;
                    case CHOICE:
                        String[] arr = new String[edit.choiceType().vl().length];
                        for (int vi = 0; vi < edit.choiceType().vl().length; vi++) {
                            arr[vi] = edit.choiceType().vl()[vi].label();
                        }
                        sheet.addValidationData(generateValidation(cellNum, SIMPLE_CELL_ERR, DVConstraint.createExplicitListConstraint(arr)));
                        break;
                    case SLIDER:
                        sheet.addValidationData(generateValidation(cellNum,
                                "请选择或输入有效的选项，区间：" + edit.sliderType().min() + " - " + edit.sliderType().max(), dvHelper.createNumericConstraint(
                                        DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.OperatorType.BETWEEN,
                                        Integer.toString(edit.sliderType().min()), Integer.toString(edit.sliderType().max()))));
                        break;
                    case DATE:
                        sheet.addValidationData(generateValidation(cellNum, "请选择或输入有效时间，或下载最新模版重试！", dvHelper.createDateConstraint(DVConstraint.OperatorType.BETWEEN,
                                "1900-01-01", "2999-12-31", "yyyy-MM-dd")));
                        break;
                    case DEPEND_SWITCH:
                        String[] dw = new String[edit.dependSwitchType().dependSwitchAttrs().length];
                        for (int vi = 0; vi < edit.dependSwitchType().dependSwitchAttrs().length; vi++) {
                            dw[vi] = edit.dependSwitchType().dependSwitchAttrs()[vi].label();
                        }
                        sheet.addValidationData(generateValidation(cellNum, SIMPLE_CELL_ERR, DVConstraint.createExplicitListConstraint(dw)));
                        break;
                    case REFERENCE_TREE:
                        ReferenceTreeType referenceTreeType = fieldModel.getEruptField().edit().referenceTreeType();
//                        eruptJpaDao.getDataMap(CoreService.ERUPTS.get(fieldModel.getFieldReturnName()), AnnotationUtil.switchFilterConditionToStr(referenceTreeType.filter()),
//                                null, Arrays.asList(referenceTreeType.id() + " as id", referenceTreeType.label() + " as label"), null);
                        Collection<TreeModel> collection = eruptDataController.getReferenceTree(eruptModel.getEruptName(), fieldModel.getFieldName(), null);
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
                if (fieldModel.getEruptField().edit().notNull()) {
                    font.setColor(Font.COLOR_RED);
                }
                style.setFont(font);
                cell.setCellStyle(style);
                //批注
                ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 1, 8, 3);
                Comment comment = drawing.createCellComment(anchor);
                comment.setString(new HSSFRichTextString(fieldModel.getFieldName()));
                cell.setCellComment(comment);
                //值
                cell.setCellValue(fieldModel.getEruptField().edit().title());
                cellNum++;
            }
        }
        try {
            wb.write(HttpUtil.downLoadFile(response, eruptModel.getErupt().name() + "_导入模板" + XLS_FORMAT));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private DataValidation generateValidation(int colIndex, String errHint, DataValidationConstraint constraint) {
        // 设置数据有效性加载在哪个单元格上。
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(1, 999, colIndex, colIndex);
        DataValidation dataValidationList = new HSSFDataValidation(regions, constraint);
        dataValidationList.createErrorBox("错误", errHint);
        return dataValidationList;
    }
}
