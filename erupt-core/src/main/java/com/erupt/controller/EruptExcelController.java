package com.erupt.controller;

import com.erupt.constant.RestPath;
import com.erupt.core.model.EruptModel;
import com.erupt.service.CoreService;
import org.springframework.web.bind.annotation.*;

/**
 * 对Excel数据的处理
 * Created by liyuepeng on 10/15/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_EXCEL)
public class EruptExcelController {


    @GetMapping("/export/{erupt}")
    @ResponseBody
    public Object exportData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().export()) {

            return null;
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
