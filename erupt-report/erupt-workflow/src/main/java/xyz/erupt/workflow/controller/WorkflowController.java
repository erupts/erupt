package xyz.erupt.workflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

/**
 * 工作流相关控制器
 *
 * @author YuePeng
 * date 2022/8/17 22:09
 */
@RestController
public class WorkflowController {

    @GetMapping("/models")
    public List<VLModel> workFlowModels() {

        return null;
    }

    public void aa() {

    }

}
