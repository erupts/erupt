package com.erupt.controller;

import com.erupt.constant.RestPath;
import com.erupt.model.core.EruptModel;
import com.erupt.service.CoreService;
import com.erupt.service.DataFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 对Excel数据的处理
 * Created by liyuepeng on 10/15/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_EXCEL)
public class EruptExcelController {

    @Autowired
    private DataFileService dataFileService;

    @GetMapping("/export/{erupt}")
    public void exportData(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().export()) {
            dataFileService.exportExcel(eruptModel, response);
        } else {
            throw new RuntimeException("没有导出权限");
        }
    }


    @PostMapping("/import/{erupt}")
    @ResponseBody
    public Object importData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().importable()) {
            return null;
        } else {
            throw new RuntimeException("没有导入权限");
        }
    }


    @GetMapping(value = "/import/template/{erupt}")
    @ResponseBody
    public Object importTemplate(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().importable()) {
            return null;
        } else {
            throw new RuntimeException("没有导入权限");
        }
    }


}
