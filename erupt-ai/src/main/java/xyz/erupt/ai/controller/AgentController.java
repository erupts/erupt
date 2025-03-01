package xyz.erupt.ai.controller;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.erupt.ai.constants.ChatType;
import xyz.erupt.ai.model.Chat;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/2/27 23:34
 */
@Component
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/agent")
public class AgentController {

    @Resource
    private EruptDao eruptDao;

    @GetMapping("/create_chat")
    @Transactional
    public void createChat(@RequestParam Long agentId) {
        LLMAgent llmAgent = eruptDao.lambdaQuery(LLMAgent.class).eq(LLMAgent::getId, agentId).one();
        Chat chat = new Chat();
        chat.setType(ChatType.AGENT);
        chat.setTypeRef(agentId);
        chat.setTypeName(llmAgent.getName());
        chat.setUserId(Long.valueOf(MetaContext.getUser().getUid()));
        eruptDao.persist(chat);
    }

    @GetMapping("/chats")
    public R<List<Chat>> chats(@RequestParam Long agentId) {
        return R.ok(eruptDao.lambdaQuery(Chat.class)
                .eq(Chat::getTypeRef, agentId)
                .eq(Chat::getUserId, Long.valueOf(MetaContext.getUser().getUid()))
                .eq(Chat::getType, ChatType.AGENT).list());
    }


}
