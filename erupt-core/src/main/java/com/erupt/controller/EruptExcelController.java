package com.erupt.controller;

import com.erupt.annotation.model.BoolAndReason;
import com.erupt.annotation.sub_field.View;
import com.erupt.base.model.EruptFieldModel;
import com.erupt.constant.RestPath;
import com.erupt.base.model.EruptModel;
import com.erupt.service.CoreService;
import com.erupt.service.DataFileService;
import com.erupt.util.HttpUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("erupt.uploadPath")
    private String uploadPath;

    @GetMapping("/export/{erupt}")
    public void exportData(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().export()) {
            dataFileService.exportExcel(eruptModel, response);
        } else {
            throw new RuntimeException("没有导出权限");
        }
    }



    //上传文件
    @PostMapping("/import/{erupt}")
    @ResponseBody
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
                return new BoolAndReason(true,null);
            } catch (IOException e) {
                e.printStackTrace();
                return new BoolAndReason(false,e.getMessage());
            }
        } else {
            throw new RuntimeException("没有导入权限");
        }
    }


    @GetMapping(value = "/import/template/{erupt}")
    @ResponseBody
    public void importTemplate(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().importable()) {

        } else {
            throw new RuntimeException("没有导入权限");
        }
    }


}
