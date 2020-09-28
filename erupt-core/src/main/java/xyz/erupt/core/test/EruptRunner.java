package xyz.erupt.core.test;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xyz.erupt.core.service.EruptApplication;

/**
 * @author liyuepeng
 * @date 2020-09-15
 */
public final class EruptRunner extends SpringJUnit4ClassRunner {

    public EruptRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        EruptApplication.run(clazz);
    }
}
