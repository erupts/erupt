package xyz.erupt.ai.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.ai.constants.AiConst;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.vo.AgentVo;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2025/3/22 23:06
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/agent")
public class AgentController {

    @Resource
    private EruptDao eruptDao;

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/list")
    public R<List<AgentVo>> list() {
        return R.ok(eruptDao.lambdaQuery(LLMAgent.class).eq(LLMAgent::getEnable, true).list().stream().map(it -> {
            AgentVo agentVo = new AgentVo();
            agentVo.setId(it.getId());
            agentVo.setName(it.getName());
            agentVo.setDesc(it.getRemark());
            if (StringUtils.isNotBlank(it.getHint())) {
                String hint = it.getHint().trim();
                if (hint.startsWith("[")) {
                    agentVo.setHints(new Gson().fromJson(hint, new TypeToken<List<String>>() {}.getType()));
                } else {
                    agentVo.setHints(Arrays.stream(hint.split("\\|")).toList());
                }
            }
            return agentVo;
        }).collect(Collectors.toList()));
    }

}
