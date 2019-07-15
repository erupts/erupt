package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.bean.Page;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.service.DataFileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Objects;

/**
 * 对Excel数据的处理
 * Created by liyuepeng on 10/15/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_EXCEL)
public class EruptExcelController {

    @Autowired
    private DataFileService dataFileService;


    @Autowired
    private EruptDataController eruptDataController;

    @Value("erupt.uploadPath:/opt/file")
    private String uploadPath;

    //导出
    @PostMapping("/export/{erupt}")
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 2)
    public void exportData(@PathVariable("erupt") String eruptName, HttpServletRequest request, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (eruptModel.getErupt().power().export()) {
            JsonObject condition = new JsonObject();
            condition.addProperty(Page.PAGE_INDEX_STR, 1);
            condition.addProperty(Page.PAGE_SIZE_STR, Page.PAGE_MAX_DATA);
            condition.addProperty(Page.PAGE_SORT_STR, "");
            Enumeration<String> en = request.getParameterNames();
            while (en.hasMoreElements()) {
                condition.addProperty(en.nextElement(), request.getParameter(en.nextElement()));
            }
            Page page = eruptDataController.getEruptData(eruptName, condition);
            dataFileService.exportExcel(eruptModel, page, response);
        } else {
            throw new RuntimeException("没有导出权限");
        }
    }

    @RequestMapping(value = "/template/{erupt}")
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 2)
    public String getExcelTemplate(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (eruptModel.getErupt().power().importable()) {
            dataFileService.createExcelTemplate(eruptModel, response);
        } else {
            throw new RuntimeException("没有导入权限");
        }
        return null;
    }


    //导入excel
    @PostMapping("/import/{erupt}")
    @ResponseBody
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 2)
    public BoolAndReason importExcel(@PathVariable("erupt") String eruptName, @RequestParam("file") MultipartFile file) throws IOException {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (eruptModel.getErupt().power().importable()) {
            if (file.isEmpty()) {
                return new BoolAndReason(false, "上传失败，请选择文件");
            }
            String fileName = file.getOriginalFilename();
            InputStream is = file.getInputStream();
            FileMagic fileMagic = FileMagic.valueOf(is);
            if (Objects.equals(fileMagic, FileMagic.OOXML) || Objects.equals(fileMagic, FileMagic.OLE2)) {
                Workbook workbook = null;
                if (fileName.endsWith(DataFileService.XLS_FORMAT)) {
                    workbook = new HSSFWorkbook(is);
                } else if (fileName.endsWith(DataFileService.XLSX_FORMAT)) {
                    workbook = new XSSFWorkbook(is);
                }
                return dataFileService.importExcel(eruptModel, workbook);
            } else {
                return new BoolAndReason(false, "上传失败，请选择Excel文件上传");
            }
        } else {
            throw new RuntimeException("没有导入权限");
        }
    }


}
