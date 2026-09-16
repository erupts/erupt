package xyz.erupt.comment.handler;

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
 * The models that actually carry comments, labelled by the data model's own name rather than its
 * class name: the comment rows are keyed by erupt name, which says nothing to whoever reads the
 * comment list. The label is the raw {@code @Erupt(name)} on purpose — EruptRecordComment is
 * {@code @EruptI18n}, so {@code EruptUtil.getChoiceList} translates every label afterwards.
 * <p>
 * Listing only the names present in the table keeps the filter short and honest: a model nobody
 * commented on is not worth an entry, and a model that has since been removed still names itself.
 * An erupt-cloud node erupt arrives as "nodeName.eruptName" and has no local class, so it keeps
 * that name as its label.
 *
 * @author YuePeng
 */
@Component
public class CommentEruptChoice implements ChoiceFetchHandler<Void> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        List<String> erupts = eruptDao.getEntityManager()
                .createQuery("select distinct c.erupt from EruptRecordComment c order by c.erupt", String.class)
                .getResultList();
        List<VLModel> list = new ArrayList<>(erupts.size());
        for (String erupt : erupts) {
            EruptModel model = EruptCoreService.getErupt(erupt);
            list.add(new VLModel(erupt, null == model ? erupt : model.getErupt().name()));
        }
        return list;
    }

}
