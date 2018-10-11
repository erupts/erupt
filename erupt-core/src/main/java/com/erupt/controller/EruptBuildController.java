package com.erupt.controller;

import com.erupt.service.CoreService;
import com.google.gson.Gson;
import org.fusesource.jansi.Ansi;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.http.converter.json.GsonFactoryBean;
import org.springframework.web.bind.annotation.*;

import static org.fusesource.jansi.Ansi.ansi;

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
    public String getEruptTableView(@PathVariable("erupt") String eruptName) {
        Gson gson = new Gson();

        System.out.println(ansi().fg(Ansi.Color.RED).a(gson.toJson(CoreService.ERUPTS.get(eruptName))));
        return gson.toJson(CoreService.ERUPTS.get(eruptName));
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
