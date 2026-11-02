package xyz.erupt.ai_dev.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_dev.constant.ReviewDecision;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.model.DevRequirementReview;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Reviews of a requirement: each approval and rejection with the reviewer's comment.
 *
 * @author YuePeng
 * date 2026/10/24
 */
@Service
public class DevReviewService {

    @Resource
    private EruptDao eruptDao;

    // newest first
    public List<DevRequirementReview> reviews(Long requirementId) {
        return eruptDao.getEntityManager().createQuery(
                        "select v from DevRequirementReview v where v.requirement.id = :id order by v.id desc", DevRequirementReview.class)
                .setParameter("id", requirementId).getResultList();
    }

    public Optional<DevRequirementReview> latest(Long requirementId) {
        return this.reviews(requirementId).stream().findFirst();
    }

    public void record(DevRequirement r, ReviewDecision decision, String comment, EruptUser reviewer) {
        DevRequirementReview v = new DevRequirementReview();
        v.setRequirement(r);
        v.setDecision(decision);
        v.setComment(StringUtils.abbreviate(StringUtils.trimToNull(comment), AnnotationConst.REMARK_LENGTH));
        v.setReviewer(reviewer);
        v.setReviewedAt(LocalDateTime.now());
        eruptDao.persist(v);
    }

    public void delete(Long requirementId) {
        eruptDao.getEntityManager().createQuery("delete from DevRequirementReview v where v.requirement.id = :id")
                .setParameter("id", requirementId).executeUpdate();
    }

}
