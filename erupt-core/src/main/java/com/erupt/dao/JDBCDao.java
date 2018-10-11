package com.erupt.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Repository
public class JDBCDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;



}
