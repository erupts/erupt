package com.erupt.service;

import com.erupt.dao.JpaDao;
import com.erupt.model.EruptModel;
import com.erupt.model.Page;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Service
@Transactional
public class DataService {


    @Autowired
    private CoreService coreService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JpaDao jpaDao;

    public void add(Object entity) {
        jpaDao.saveEntity(entity);
    }

    public void delete(Object entity) {

    }

    public void edit(Object entity) {

    }

    public void query(Class<?> clazz) {

    }
}
