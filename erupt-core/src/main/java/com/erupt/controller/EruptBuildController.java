package com.erupt.controller;

import com.erupt.model.EruptModel;
import com.erupt.service.CoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Erupt 页面结构构建信息
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping("/erupt-api/build")
public class EruptBuildController {

    @Autowired
    private CoreService startService;

    @GetMapping("/list/{erupt}")
    @ResponseBody
    public void getEruptTableView(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        eruptModel.getEruptFields().forEach(field -> {

        });
    }

    @GetMapping("/row/edit/{erupt}")
    @ResponseBody
    public void getEruptAddForms(@PathVariable("erupt") String erupt) {

    }

    @GetMapping("/row/edit/{erupt}/{id}")
    @ResponseBody
    public void getEruptEditForms(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {

    }


}
