package xyz.erupt.decision.core;

import org.springframework.stereotype.Component;

/**
 * TypeSafe's Jev, the first System One model. Hosted, metered, keyed.
 *
 * @author YuePeng
 */
@Component
public class Jev extends HttpDecisionCore {

    @Override
    public String code() {
        return "TypeSafe Jev";
    }

    @Override
    public String api() {
        return "https://api.typesafe.ai";
    }

    @Override
    public String model() {
        return "jev-latest";
    }

}
