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
        eruptUser.setPassword(null);
        return GsonFactory.getGson().toJson(eruptUser);
    }

    @Tool("查询 Erupt 实体数据。注意：必须使用 HQL (Hibernate Query Language)，" +
            "针对 Java 实体类名而非表名进行查询。例如：'from User where name = :name'")
    public String eruptDataQuery(@P("面向对象的 HQL 语句。格式参考：SELECT e FROM [实体类名] e WHERE e.[属性名] = ...")
                                 String hql) {
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
