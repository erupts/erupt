package com.erupt.service;

import com.erupt.dao.JpaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Service
@Transactional
public class DataService {
    @Autowired
    private JpaDao jpaDao;


}
