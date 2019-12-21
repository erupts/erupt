package xyz.erupt.annotation.fun;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.sub_erupt.Power;

public interface PowerHandler {

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
