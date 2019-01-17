package xyz.erupt.eruptconfig.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by liyuepeng on 2019-01-17.
 */
@Service
public class EruptConfigService implements InitializingBean {

    @Autowired
    private Environment env;

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
