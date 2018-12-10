package com.erupt.entity;

import com.erupt.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by liyuepeng on 12/7/18.
 */
@Entity
@Table(name = "E_DICT")
public class EruptDict extends BaseModel {

    private String code;

    private String name;

    @ManyToOne
    @JoinColumn(name = "PARENT_DICT_ID")
    private EruptDict eruptDict;

    private String sort;

    private String remark;
}
