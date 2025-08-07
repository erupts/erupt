package xyz.erupt.excel.controller;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.controller.EruptModifyController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.naming.EruptRecordNaming;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.SecurityUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.excel.service.EruptExcelService;
import xyz.erupt.excel.util.ExcelUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 对Excel数据的处理
 *
 * @author YuePeng
 * date 10/15/18.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_EXCEL)
@RequiredArgsConstructor
@Slf4j
public class EruptExcelController {

    private final EruptProp eruptProp;

    private final EruptExcelService dataFileService;

    private final EruptModifyService eruptModifyService;

    private final EruptService eruptService;

    private final EruptModifyController eruptModifyController;

    //模板下载
    @GetMapping(value = "/template/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public void getExcelTemplate(@PathVariable("erupt") String eruptName, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) return;
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isImportable);
        try (Workbook wb = dataFileService.createExcelTemplate(eruptModel)) {
            wb.write(ExcelUtil.downLoadFile(request, response, eruptModel.getErupt().name() + "_template" + EruptExcelService.XLS_FORMAT));
        }
    }

    //导出
    @PostMapping("/export/{erupt}")
    @EruptRecordOperate(value = "Export Excel", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public void exportData(@PathVariable("erupt") String eruptName,
                           @RequestBody(required = false) List<Condition> conditions,
                           HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) return;
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isExport);
        TableQuery tableQuery = new TableQuery();
        tableQuery.setPageIndex(1);
        tableQuery.setPageSize(Page.PAGE_MAX_DATA);
        Optional.ofNullable(conditions).ifPresent(tableQuery::setCondition);
        Page page = eruptService.getEruptData(eruptModel, tableQuery, null);
        try (Workbook wb = dataFileService.exportExcel(eruptModel, page)) {
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.excelExport(wb)));
            this.createConditionSheet(wb, eruptModel, conditions);
            DateUtil.getSimpleFormatDateTime(new Date());
            wb.write(ExcelUtil.downLoadFile(request, response, eruptModel.getErupt().name()
                    + "_" + DateUtil.getFormatDate(new Date(), "yyyy-MM-dd_HH-mm-ss") + EruptExcelService.XLSX_FORMAT));
        }
    }

    private void createConditionSheet(Workbook wb, EruptModel eruptModel, List<Condition> conditions) {
        Sheet sheet = wb.createSheet("condition");
        sheet.createFreezePane(0, 1, 1, 1);
        sheet.setColumnWidth(0, 16 * 256);
        sheet.setColumnWidth(1, 12 * 256);
        sheet.setColumnWidth(2, 50 * 256);
        Row head = sheet.createRow(sheet.getLastRowNum() + 1);
        head.createCell(0).setCellValue("name");
        head.createCell(1).setCellValue("expr");
        head.createCell(2).setCellValue("value");
        if (null != conditions) {
            conditions.forEach(condition -> {
                if (null != condition.getValue()) {
                    EruptField eruptField = eruptModel.getEruptFieldMap().get(condition.getKey()).getEruptField();
                    if (eruptField.views().length > 0) {
                        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                        row.createCell(0).setCellValue(eruptField.views()[0].title());
                        row.createCell(1).setCellValue(null == condition.getExpression() ? QueryExpression.EQ.name() : condition.getExpression().name());
                        row.createCell(2).setCellValue(condition.getValue().toString());
                    }
                }
            });
        }
    }

    //导入
    @PostMapping("/import/{erupt}")
    @EruptRecordOperate(value = "Import Excel", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public EruptApiModel importExcel(@PathVariable("erupt") String eruptName, @RequestParam("file") MultipartFile file) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isImportable, "Not import permission");
        if (file.isEmpty() || null == file.getOriginalFilename()) return EruptApiModel.errorApi("No file");
        List<JsonObject> list;
        int i = 1;
        try {
            i++;
            Workbook wb;
            if (file.getOriginalFilename().endsWith(EruptExcelService.XLS_FORMAT)) {
                wb = new HSSFWorkbook(file.getInputStream());
            } else if (file.getOriginalFilename().endsWith(EruptExcelService.XLSX_FORMAT)) {
                wb = new XSSFWorkbook(file.getInputStream());
            } else {
                throw new EruptWebApiRuntimeException("The uploaded file format must be Excel");
            }
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.excelImport(wb)));
            list = dataFileService.excelToEruptObject(eruptModel, wb);
            wb.close();
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException("Excel解析异常，出错行数：" + i + "，原因：" + e.getMessage(), e);
        }
        try {
            List<Object> eruptDataList = new ArrayList<>();
            int j = 1;
            for (JsonObject data : list) {
                j++;
                EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);
                if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {
                    throw new EruptWebApiRuntimeException("第" + j + "行，" + eruptApiModel.getMessage());
                }
                eruptDataList.add(eruptModifyService.eruptInsertDataProcess(eruptModel, data));
            }
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.excelImportProcess(eruptDataList)));
            eruptModifyController.batchAddEruptData(eruptModel, eruptDataList);
        } catch (Exception e) {
            log.error("import error {}", eruptModel.getEruptName(), e);
            throw new EruptWebApiRuntimeException("数据导入异常，原因：" + e.getMessage());
        }
        return EruptApiModel.successApi();
    }


}
