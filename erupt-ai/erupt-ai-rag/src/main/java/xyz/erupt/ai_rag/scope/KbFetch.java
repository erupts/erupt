package xyz.erupt.ai_rag.scope;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.model.KnowledgeBase;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * The knowledge bases the current user may pick, by name.
 *
 * @author YuePeng
 */
@Component
public class KbFetch implements ChoiceFetchHandler<Void> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        return eruptDao.lambdaQuery(KnowledgeBase.class).orderByAsc(KnowledgeBase::getName).list().stream()
                .map(it -> new VLModel(it.getId().toString(), it.getName())).toList();
    }

}
