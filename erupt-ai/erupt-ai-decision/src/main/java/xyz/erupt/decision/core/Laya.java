package xyz.erupt.decision.core;

import org.springframework.stereotype.Component;

/**
 * Convai Innovations' Laya, an open-weights System One model that runs on your own hardware
 * and answers in Jev's wire shape. The python package ships no HTTP server, so erupt provides
 * one: see <code>laya/</code> in this module for the sidecar and its Dockerfile. It listens
 * on <code>/v1/systemone</code>, needs no key unless you give the sidecar one, and treats the
 * model name as the checkpoint to use — <code>auto</code> lets Laya's router pick by language.
 *
 * @author YuePeng
 */
@Component
public class Laya extends HttpDecisionCore {

    @Override
    public String code() {
        return "Laya";
    }

    @Override
    public String api() {
        return "http://127.0.0.1:8000";
    }

    @Override
    public String model() {
        return "auto";
    }

    @Override
    protected boolean keyRequired() {
        return false;
    }

}
