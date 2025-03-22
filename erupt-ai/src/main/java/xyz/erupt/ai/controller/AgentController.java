package xyz.erupt.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.annotation.EruptLoginAuth;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/3/22 23:06
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/agent")
public class AgentController {

    @Resource
    private EruptDao eruptDao;

    @EruptLoginAuth
    @GetMapping("/list")
    public R<Void> list() {
        List<LLMAgent> agents = eruptDao.lambdaQuery(LLMAgent.class).eq(LLMAgent::getEnable, true).list();

        return R.ok();
    }

}
