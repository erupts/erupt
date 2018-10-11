package com.erupt.service;

import com.erupt.model.EruptModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Service
public class DataService {


    @Autowired
    private CoreService coreService;

    public boolean add(EruptModel eruptModel, Object object) {
        if (eruptModel.getErupt().power().add()) {
            return false;
        }
        throw new RuntimeException("没有新增权限");
    }

    public void delete() {

    }

    public void edit() {

    }

    public void query(EruptModel eruptModel) {
        if (eruptModel.getErupt().power().add()) {

        }
        throw new RuntimeException("没有查询权限");
    }
}
