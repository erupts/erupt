package com.erupt.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by liyuepeng on 12/7/18.
 */
@Entity
@Table(name = "E_DICT")
public class EruptDict {

    private String code;

    private String name;

    private EruptDict eruptDict;

    private String sort;

    private String remark;
}
