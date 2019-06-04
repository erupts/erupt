package xyz.erupt.core.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditTypeMapping;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.HttpUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by liyuepeng on 12/4/18.
 */
@Service
public class DataFileService {

    @Autowired
    private EruptJpaDao eruptJpaDao;

    public static final String SIMPLE_CELL_ERR = "请选择或输入有效的选项，或下载最新模版重试！";

    //展示的格式和view表格一致
    //excel导出
    public void exportExcel(EruptModel eruptModel, HttpServletResponse response) {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(eruptModel.getErupt().name());
        sheet.createFreezePane(0, 1, 1, 1);
        Row row = sheet.createRow(0);
        int colNum = 0;
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            for (View view : fieldModel.getEruptField().views()) {
                if (view.show()) {
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
        try {
            wb.write(HttpUtil.downLoadFile(response, eruptModel.getErupt().name() + ".xls"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                "数值区间：" + edit.sliderType().min() + "——" + edit.sliderType().min(), dvHelper.createNumericConstraint(
                                        DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.OperatorType.BETWEEN,
                                        Integer.valueOf(edit.sliderType().min()).toString(), Integer.valueOf(edit.sliderType().max()).toString())));
                        break;
                    case DATE:
                        sheet.addValidationData(generateValidation(cellNum, "请选择或输入有效时间，或下载最新模版重试！", dvHelper.createDateConstraint(DVConstraint.OperatorType.BETWEEN,
                                "1000-01-01", "2999-12-31", "yyyy-MM-dd")));
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
            wb.write(HttpUtil.downLoadFile(response, eruptModel.getErupt().name() + "_导入模板" + ".xls"));
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
        dataValidationList.createErrorBox("Error", errHint);
        return dataValidationList;
    }
}
