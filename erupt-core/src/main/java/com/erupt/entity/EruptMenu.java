package com.erupt.entity;

import com.erupt.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Entity
@Table(name = "E_MENU")
public class EruptMenu extends BaseModel {

    private String name;

    private String sort;

    private boolean isShow;

    private String path;

    private EruptMenu eruptMenu;

    private String icon;

    private String target;

    private String remark;


}
