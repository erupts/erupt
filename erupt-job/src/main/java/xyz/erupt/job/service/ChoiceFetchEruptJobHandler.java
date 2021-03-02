package xyz.erupt.job.service;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2021/2/27 22:46
 */
public class ChoiceFetchEruptJobHandler implements ChoiceFetchHandler {

    @Override
    public List<VLModel> fetch(String[] params) {
        List<VLModel> list = new ArrayList<>();
        for (String job : JobDataLoadService.getLoadedJobHandler()) {
            list.add(new VLModel(job, job.substring(job.lastIndexOf(".") + 1)));
        }
        return list;
    }

}
