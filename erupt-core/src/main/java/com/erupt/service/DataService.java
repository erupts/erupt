package com.erupt.service;

import com.erupt.annotation.sub_erupt.Tree;
import com.erupt.dao.JpaDao;
import com.erupt.model.EruptModel;
import com.erupt.model.TreeModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Service
@Transactional
public class DataService {
    @Autowired
    private JpaDao jpaDao;


}
