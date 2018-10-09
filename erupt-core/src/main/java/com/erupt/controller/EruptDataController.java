package com.erupt.controller;

import com.erupt.model.EruptModel;
import org.springframework.web.bind.annotation.*;

/**
 * Erupt 对数据的增删改查
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping("/erupt-api/data")
public class EruptDataController {


    @GetMapping("/{erupt}")
    @ResponseBody
    public Object getErupt(@PathVariable("erupt") String erupt) {
        return null;
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    public void addErupt(@PathVariable("erupt") String erupt) {

    }

    @PutMapping("/{erupt}/{id}")
    @ResponseBody
    public void editErupt(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {

    }

    @DeleteMapping("/{erupt}/{id}")
    @ResponseBody
    public void deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {

    }


}
