package com.erupt.controller;

import com.erupt.constant.RestPath;
import com.erupt.dao.EruptJpaDao;
import com.erupt.dao.JpaDao;
import com.erupt.model.EruptModel;
import com.erupt.model.Page;
import com.erupt.service.CoreService;
import com.erupt.service.DataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

/**
 * Erupt 对数据的增删改查
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_DATA)
@Transactional
public class EruptDataController {

    @Autowired
    private DataService dataService;

    @Autowired
    private JpaDao jpaDao;

    @Autowired
    private EruptJpaDao eruptJpaDao;

    private Gson gson = new Gson();

    @PostMapping("/query/{erupt}")
    @ResponseBody
    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody JsonObject data) throws JsonProcessingException {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        String dataStr = gson.toJson(data);
        JsonObject conditionParam = null;
        JsonObject pageJson = null;
        if (StringUtils.isNotBlank(dataStr)) {
            JsonObject jo = new JsonParser().parse(dataStr).getAsJsonObject();
            conditionParam = jo.getAsJsonObject(EruptJpaDao.CONDITION_KEY);
        }
        if (eruptModel.getErupt().power().query()) {
            Page page = eruptJpaDao.queryEruptList(eruptModel, conditionParam, new Page(1, 3));
            return page;
        } else {
            throw new RuntimeException("没有查询权限");
        }
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    public void addEruptData(@PathVariable("erupt") String erupt, @RequestBody Object data) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            eruptJpaDao.saveEntity(eruptModel, gson.fromJson(gson.toJson(data), eruptModel.getClazz()));
        } else {
            throw new RuntimeException("没有新增权限");
        }
    }

    @PutMapping("/{erupt}/{id}")
    @ResponseBody
    public void editEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
//            eruptJpaDao.findDataById(eruptModel, id);
        } else {
            throw new RuntimeException("没有修改权限");
        }
    }

    @DeleteMapping("/{erupt}/{id}")
    @ResponseBody
    public void deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") Serializable id) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            eruptJpaDao.deleteEntity(eruptJpaDao.findDataById(eruptModel, id));
        } else {
            throw new RuntimeException("没有删除权限");
        }
    }

    //为了事务性所以增加了批量删除功能
    @DeleteMapping("/{erupt}")
    @ResponseBody
    public void deleteEruptDatas(@PathVariable("erupt") String erupt, @RequestParam("ids") Serializable[] ids) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            for (Serializable id : ids) {
                eruptJpaDao.deleteEntity(eruptJpaDao.findDataById(eruptModel, id));
            }
        } else {
            throw new RuntimeException("没有删除权限");
        }
    }


    @GetMapping("/{erupt}/ref/{name}")
    @ResponseBody
    public List getRefData(@PathVariable("erupt") String erupt, @PathVariable("name") String name) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        return eruptJpaDao.getReferenceList(eruptModel, name);
    }


}
