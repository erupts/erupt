package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.service.DataFileService;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

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
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void exportData(@PathVariable("erupt") String eruptName, @RequestBody JsonObject condition, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().export()) {
            condition.addProperty(Page.PAGE_INDEX_STR, 1);
            condition.addProperty(Page.PAGE_SIZE_STR, Page.PAGE_MAX_DATA);
            Page page = eruptDataController.getEruptData(eruptName, condition);
            dataFileService.exportExcel(eruptModel, page, response);
        } else {
            throw new RuntimeException("没有导出权限");
        }
    }

    @GetMapping(value = "/template/{erupt}")
    @EruptRouter(verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public String getExcelTemplate(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().importable()) {
            try {
                dataFileService.createExcelTemplate(eruptModel, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("没有导入权限");
        }
        return null;
    }


    //导入excel
    @PostMapping("/import/{erupt}")
    @ResponseBody
    @EruptRouter
    public BoolAndReason importData(@PathVariable("erupt") String eruptName, @RequestParam("file") MultipartFile file) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().importable()) {
            if (file.isEmpty()) {
                return new BoolAndReason(false, "上传失败，请选择文件");
            }
            String fileName = file.getOriginalFilename();
            File dest = new File(this.uploadPath + fileName);
            try {
                file.transferTo(dest);
                //TODO 读取上传后的文件做数据上传工作
                return new BoolAndReason(true, null);
            } catch (IOException e) {
                e.printStackTrace();
                return new BoolAndReason(false, e.getMessage());
            }
        } else {
            throw new RuntimeException("没有导入权限");
        }
    }


}
