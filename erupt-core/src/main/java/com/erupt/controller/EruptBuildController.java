package com.erupt.controller;


import com.erupt.constant.RestPath;
import com.erupt.base.model.EruptModel;
import com.erupt.service.CoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Erupt 页面结构构建信息
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_BUILD)
public class EruptBuildController {

    @GetMapping("/list/{erupt}")
    @ResponseBody
    public EruptModel getEruptTableView(@PathVariable("erupt") String eruptName) {
        return CoreService.ERUPTS.get(eruptName);
    }


}
