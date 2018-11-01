package com.erupt.controller;

import com.erupt.constant.RestPath;
import com.erupt.dao.EruptJpaDao;
import com.erupt.dao.JpaDao;
import com.erupt.model.EruptModel;
import com.erupt.model.Page;
import com.erupt.service.CoreService;
import com.erupt.service.DataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

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

    @GetMapping("/{erupt}")
    @ResponseBody
    public Page getErupt(@PathVariable("erupt") String eruptName) throws JsonProcessingException {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            Page page = eruptJpaDao.queryByPage(eruptModel, new Page(1, 3));
            return page;
        } else {
            throw new RuntimeException("没有查询权限");
        }
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    public void addErupt(@PathVariable("erupt") String erupt) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {

        } else {
            throw new RuntimeException("没有新增权限");
        }
    }

    @PutMapping("/{erupt}/{id}")
    @ResponseBody
    public void editErupt(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
//            eruptJpaDao.findDataById(eruptModel, id);
        } else {
            throw new RuntimeException("没有修改权限");
        }
    }

    @DeleteMapping("/{erupt}/{id}")
    @ResponseBody
    public void deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            eruptJpaDao.deleteEntity(eruptJpaDao.findDataById(eruptModel, id));
        } else {
            throw new RuntimeException("没有删除权限");
        }
    }


}
