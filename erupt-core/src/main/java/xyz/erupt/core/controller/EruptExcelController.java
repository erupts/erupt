package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptExcelService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.HttpUtil;
import xyz.erupt.core.util.SecurityUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

/**
 * 对Excel数据的处理
 *
 * @author liyuepeng
 * @date 10/15/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_EXCEL)
public class EruptExcelController {


    @Autowired
    private EruptProp eruptProp;

    @Autowired
    private EruptExcelService dataFileService;

    @Autowired
    private EruptDataController eruptDataController;

    @Autowired
    private EruptModifyController eruptModifyController;


    //模板下载
    @RequestMapping(value = "/template/{erupt}")
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public void getExcelTemplate(@PathVariable("erupt") String eruptName, HttpServletRequest request, HttpServletResponse response) {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) {
            return;
        }
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        if (EruptUtil.getPowerObject(eruptModel).isImportable()) {
            dataFileService.createExcelTemplate(eruptModel, request, response);
        } else {
            throw new EruptWebApiRuntimeException("没有导入权限");
        }
    }

    //导出
    @PostMapping("/export/{erupt}")
    @EruptRecordOperate(desc = "导出Excel")
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public void exportData(@PathVariable("erupt") String eruptName, @RequestParam("condition") String condition,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) {
            return;
        }
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        if (EruptUtil.getPowerObject(eruptModel).isExport()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Page.PAGE_INDEX_STR, 1);
            jsonObject.addProperty(Page.PAGE_SIZE_STR, Page.PAGE_MAX_DATA);
            jsonObject.addProperty(Page.PAGE_SORT_STR, "");
            JsonObject jsonCondition = JsonParser.parseString(
                    URLDecoder.decode(condition, "utf-8")).getAsJsonObject();
            for (String key : jsonCondition.keySet()) {
                jsonObject.add(key, jsonCondition.get(key));
            }
            Page page = eruptDataController.getEruptData(eruptName, jsonObject);
            Workbook wb = dataFileService.exportExcel(eruptModel, page);
            EruptUtil.handlerDataProxy(eruptModel, (dataProxy -> dataProxy.excelExport(wb)));
            wb.write(HttpUtil.downLoadFile(request, response, eruptModel.getErupt().name() + EruptExcelService.XLS_FORMAT));
        } else {
            throw new EruptWebApiRuntimeException("没有导出权限");
        }
    }

    //导入
    @PostMapping("/import/{erupt}")
    @EruptRecordOperate(desc = "导入Excel")
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional(rollbackOn = Exception.class)
    public EruptApiModel importExcel(@PathVariable("erupt") String eruptName, @RequestParam("file") MultipartFile file) throws Exception {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        if (EruptUtil.getPowerObject(eruptModel).isImportable()) {
            if (file.isEmpty()) {
                return EruptApiModel.errorApi("上传失败，请选择文件");
            }
            String fileName = file.getOriginalFilename();
            List<JsonObject> list;
            int i = 1;
            try {
                i++;
                if (fileName.endsWith(EruptExcelService.XLS_FORMAT)) {
                    list = dataFileService.excelToEruptObject(eruptModel, new HSSFWorkbook(file.getInputStream()));
                } else if (fileName.endsWith(EruptExcelService.XLSX_FORMAT)) {
                    list = dataFileService.excelToEruptObject(eruptModel, new XSSFWorkbook(file.getInputStream()));
                } else {
                    throw new EruptWebApiRuntimeException("上传文件必须为Excel");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new EruptWebApiRuntimeException("Excel解析异常" + "，出错行数：" + i + "，原因：" + e.getMessage());
            }
            i = 1;
            for (JsonObject jo : list) {
                i++;
                EruptApiModel eruptApiModel = eruptModifyController.addEruptData(eruptName, jo, null);
                if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {
                    throw new EruptWebApiRuntimeException("第" + i + "行：" + eruptApiModel.getMessage());
                }
            }
            return EruptApiModel.successApi();
        } else {
            throw new EruptWebApiRuntimeException("没有导入权限");
        }
    }


}
