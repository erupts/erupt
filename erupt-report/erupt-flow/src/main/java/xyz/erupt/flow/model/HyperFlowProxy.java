package xyz.erupt.flow.model;

import xyz.erupt.annotation.fun.DataProxy;

/**
 * @author YuePeng
 * date 2021/1/18 17:11
 */
public class HyperFlowProxy implements DataProxy<HyperFlow> {

    @Override
    public String beforeFetch() {
        return null;
    }
}
