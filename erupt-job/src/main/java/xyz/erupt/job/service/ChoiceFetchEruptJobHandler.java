package xyz.erupt.job.service;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

/**
 * @author YuePeng
 * date 2021/2/27 22:46
 */
public class ChoiceFetchEruptJobHandler implements ChoiceFetchHandler {

    @Override
    public List<VLModel> fetch(String[] params) {
        return JobDataLoadService.getLoadedJobHandler();
    }

}
