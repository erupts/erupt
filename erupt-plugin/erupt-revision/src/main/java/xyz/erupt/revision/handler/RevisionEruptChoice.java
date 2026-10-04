package xyz.erupt.revision.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.ArrayList;
import java.util.List;

/**
 * The models that actually carry revisions, labelled by the data model's own name rather than
 * its class name. The label is the raw {@code @Erupt(name)} on purpose: EruptRecordRevision is
 * {@code @EruptI18n}, so {@code EruptUtil.getChoiceList} translates every label afterwards.
 * A model that has since been removed still names itself.
 *
 * @author YuePeng
 */
@Component
public class RevisionEruptChoice implements ChoiceFetchHandler<Void> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        List<String> erupts = eruptDao.getEntityManager()
                .createQuery("select distinct r.erupt from EruptRecordRevision r order by r.erupt", String.class)
                .getResultList();
        List<VLModel> list = new ArrayList<>(erupts.size());
        for (String erupt : erupts) {
            EruptModel model = EruptCoreService.getErupt(erupt);
            list.add(new VLModel(erupt, null == model ? erupt : model.getErupt().name()));
        }
        return list;
    }

}
