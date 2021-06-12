package xyz.erupt.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.naming.EruptOperateConfig;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptExcelService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.HttpUtil;
import xyz.erupt.core.util.SecurityUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQueryVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 对Excel数据的处理
 *
 * @author YuePeng
 * date 10/15/18.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_EXCEL)
@RequiredArgsConstructor
public class EruptExcelController {

    private final EruptProp eruptProp;

    private final EruptExcelService dataFileService;

    private final EruptModifyController eruptModifyController;

    private final EruptService eruptService;

    //模板下载
    @RequestMapping(value = "/template/{erupt}")
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public void getExcelTemplate(@PathVariable("erupt") String eruptName, HttpServletRequest request, HttpServletResponse response) {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) {
            return;
        }
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        if (PowerInvoke.getPowerObject(eruptModel).isImportable()) {
            dataFileService.createExcelTemplate(eruptModel, request, response);
        } else {
            throw new EruptWebApiRuntimeException("没有导入权限");
        }
    }

    //导出
    @PostMapping("/export/{erupt}")
    @EruptRecordOperate(value = "导出Excel", dynamicConfig = EruptOperateConfig.class)
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public void exportData(@PathVariable("erupt") String eruptName,
                           String condition,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) {
            return;
        }
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        if (PowerInvoke.getPowerObject(eruptModel).isExport()) {
            TableQueryVo tableQueryVo = new TableQueryVo();
            tableQueryVo.setPageIndex(1);
            tableQueryVo.setDataExport(true);
            if (null != condition) {
                List<Condition> conditions = new Gson().fromJson(URLDecoder.decode(condition, StandardCharsets.UTF_8.name()),
                        new TypeToken<List<Condition>>() {
                        }.getType());
                tableQueryVo.setCondition(conditions);
            }
            Page page = eruptService.getEruptData(eruptModel, tableQueryVo, null);
            Workbook wb = dataFileService.exportExcel(eruptModel, page);
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.excelExport(wb)));
            wb.write(HttpUtil.downLoadFile(request, response, eruptModel.getErupt().name() + EruptExcelService.XLS_FORMAT));
        } else {
            throw new EruptWebApiRuntimeException("没有导出权限");
        }
    }

    //导入
    @PostMapping("/import/{erupt}")
    @EruptRecordOperate(value = "导入Excel", dynamicConfig = EruptOperateConfig.class)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional(rollbackOn = Exception.class)
    public EruptApiModel importExcel(@PathVariable("erupt") String eruptName, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        if (PowerInvoke.getPowerObject(eruptModel).isImportable()) {
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
                    throw new EruptWebApiRuntimeException("上传文件格式必须为Excel");
                }
            } catch (Exception e) {
                throw new EruptWebApiRuntimeException("Excel解析异常，出错行数：" + i + "，原因：" + e.getMessage(), e);
            }
            i = 1;
            for (JsonObject jo : list) {
                i++;
                EruptApiModel eruptApiModel = eruptModifyController.addEruptData(eruptName, jo, null, request);
                if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {
                    throw new EruptWebApiRuntimeException("数据入库异常，出错行数：" + i + "，原因：" + eruptApiModel.getMessage());
                }
            }
            return EruptApiModel.successApi();
        } else {
            throw new EruptWebApiRuntimeException("没有导入权限");
        }
    }


}
