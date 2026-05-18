package xyz.erupt.ai.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.core.AiToolboxManager;
import xyz.erupt.ai.model.LLMRole;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 */
@Service
public class LLMRoleService {

    @Resource
    private EruptDao eruptDao;

    private List<LLMRole> queryByRoleCodes(List<String> roleCodes) {
        return eruptDao.lambdaQuery(LLMRole.class)
                .eq(LLMRole::getEnable, true)
                .with(LLMRole::getRole).in(EruptRole::getCode, roleCodes).with()
                .list();
    }

    public Set<String> getAllowedToolsByUid(Long uid) {
        if (uid == null) return Set.of();
        EruptUser user = eruptDao.find(EruptUser.class, uid);
        if (user == null) return Set.of();
        if (Boolean.TRUE.equals(user.getIsAdmin())) return AiToolboxManager.getAiMethodMap().keySet();
        List<String> roleCodes = user.getRoles().stream().map(EruptRole::getCode).collect(Collectors.toList());
        if (roleCodes.isEmpty()) return Set.of();
        List<LLMRole> configs = this.queryByRoleCodes(roleCodes);
        if (configs.isEmpty()) return Set.of();
        return configs.stream()
                .filter(c -> c.getTools() != null)
                .flatMap(c -> c.getTools().stream())
                .collect(Collectors.toSet());
    }

    public String getSystemPromptByUid(Long uid) {
        if (uid == null) return null;
        EruptUser user = eruptDao.find(EruptUser.class, uid);
        if (user == null) return null;
        List<String> roleCodes = user.getRoles().stream().map(EruptRole::getCode).collect(Collectors.toList());
        if (roleCodes.isEmpty()) return null;
        return this.queryByRoleCodes(roleCodes).stream()
                .map(LLMRole::getSystemPrompt)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.joining("\n\n"));
    }

}
