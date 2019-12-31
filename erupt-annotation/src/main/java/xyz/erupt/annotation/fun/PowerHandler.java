package xyz.erupt.annotation.fun;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.sub_erupt.Power;

/**
 * @author liyuepeng
 * @date 2019-11-25.
 */
public interface PowerHandler {

    /**
     * erupt权限执行类
     *
     * @param power 注解
     * @return power bean
     */
    PowerBean handler(Power power);

    @Getter
    @Setter
    class PowerBean {
        private boolean add;

        private boolean delete;

        private boolean edit;

        private boolean query;

        private boolean viewDetails;

        private boolean export;

        private boolean importable;
    }

}
