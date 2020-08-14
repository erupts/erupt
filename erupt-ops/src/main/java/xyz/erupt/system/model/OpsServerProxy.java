package xyz.erupt.system.model;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;

/**
 * @author liyuepeng
 * @date 12/7/18.
 */

@Component
public class OpsServerProxy implements DataProxy<OpsServer> {

    @Override
    public void beforeAdd(OpsServer opsServer) {

    }

    @Override
    public void beforeUpdate(OpsServer opsServer) {

    }


}
