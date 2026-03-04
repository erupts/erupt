package xyz.erupt.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiToolbox;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;

import java.util.List;

@AiToolbox
@Component
public class EruptAiToolbox {

    @Resource
    private EruptDao eruptDao;

    @Tool("Erupt model list")
    public String eruptModelList() {
        StringBuilder sb = new StringBuilder();
        for (EruptModel erupt : EruptCoreService.getErupts()) {
            sb.append(erupt.getEruptName()).append(": ").append(erupt.getErupt().name());
        }
        return sb.toString();
    }

    @Tool("Erupt model schema")
    public String eruptSchema(@P("Erupt Name") String eruptName) {
        EruptModel erupt = EruptCoreService.getEruptView(eruptName);
        return GsonFactory.getGson().toJson(erupt);
    }

    @Tool("Ask the current system logged-in user")
    public String eruptUserInfo() {
        EruptUser eruptUser = eruptDao.find(EruptUser.class, MetaContext.getUser().getUid());
        eruptUser.setRoles(null);
        eruptUser.setHeadOrg(null);
        eruptUser.setPassword(null);
        return GsonFactory.getGson().toJson(eruptUser);
    }

    @Tool("Query erupt model data")
    public String eruptDataQuery(@P("HQL (Hibernate Query Language)") String hql) {
        List<?> result = eruptDao.getEntityManager().createQuery(hql).getResultList();
        return GsonFactory.getGson().toJson(result);
    }

    @Tool("Erupt Module list")
    public String eruptModuleList() {
        StringBuilder sb = new StringBuilder();
        EruptModuleInvoke.invoke(it -> sb.append(GsonFactory.getGson().toJson(it.info())).append("\n"));
        return sb.toString();
    }

}
