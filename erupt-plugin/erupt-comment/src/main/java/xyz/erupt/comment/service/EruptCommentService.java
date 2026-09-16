package xyz.erupt.comment.service;

import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.comment.model.EruptRecordComment;
import xyz.erupt.comment.pojo.CommentInput;
import xyz.erupt.comment.pojo.CommentVo;
import xyz.erupt.comment.pojo.MentionVo;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EruptCommentService {

    private static final int MAX_LENGTH = 4000;

    private static final int MENTION_CANDIDATES = 20;

    private static final Gson GSON = new Gson();

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    // present only when the erupt-notice module is on the classpath
    @Autowired
    private ObjectProvider<CommentMentionNotifier> mentionNotifier;

    public List<CommentVo> list(String erupt, String recordId) {
        Long uid = eruptUserService.getCurrentUid();
        return eruptDao.lambdaQuery(EruptRecordComment.class)
                .eq(EruptRecordComment::getErupt, erupt)
                .eq(EruptRecordComment::getRecordId, recordId)
                .orderByAsc(EruptRecordComment::getCreateTime)
                .list().stream().map(it -> CommentVo.of(it, uid)).toList();
    }

    // Number of comments per record for the rows of one table page, so the table can show a badge.
    public Map<String, Long> counts(String erupt, List<String> recordIds) {
        Map<String, Long> counts = new HashMap<>();
        if (null == recordIds || recordIds.isEmpty()) return counts;
        List<Object[]> rows = eruptDao.getEntityManager().createQuery(
                        "select c.recordId, count(c) from EruptRecordComment c where c.erupt = :erupt and c.recordId in :ids group by c.recordId",
                        Object[].class)
                .setParameter("erupt", erupt)
                .setParameter("ids", recordIds)
                .getResultList();
        for (Object[] row : rows) counts.put((String) row[0], (Long) row[1]);
        return counts;
    }

    // Enabled users whose name contains the keyword, offered while typing an @mention.
    public List<MentionVo> mentionCandidates(String keyword) {
        String kw = null == keyword ? "" : keyword.trim();
        return eruptDao.lambdaQuery(EruptUserVo.class)
                .eq(EruptUserVo::getStatus, true)
                .like(!kw.isEmpty(), EruptUserVo::getName, kw)
                .orderByAsc(EruptUserVo::getName)
                .limit(MENTION_CANDIDATES)
                .list().stream().map(u -> new MentionVo(u.getId(), u.getName(), u.getAvatar())).toList();
    }

    @Transactional
    public CommentVo add(String erupt, String recordId, CommentInput input) {
        this.checkEnabled(erupt);
        String content = null == input.getContent() ? "" : input.getContent().trim();
        if (content.isEmpty()) throw new EruptWebApiRuntimeException("Comment content is empty");
        if (content.length() > MAX_LENGTH) throw new EruptWebApiRuntimeException("Comment content is too long");
        EruptRecordComment comment = new EruptRecordComment();
        comment.setErupt(erupt);
        comment.setRecordId(recordId);
        comment.setContent(content);
        if (null != input.getParentId()) {
            EruptRecordComment parent = eruptDao.find(EruptRecordComment.class, input.getParentId());
            if (null == parent || !erupt.equals(parent.getErupt()) || !recordId.equals(parent.getRecordId())) {
                throw new EruptWebApiRuntimeException("Reply target not found");
            }
            // keep threads one level deep
            comment.setParentId(null == parent.getParentId() ? parent.getId() : parent.getParentId());
        }
        List<MentionVo> mentions = this.resolveMentions(input.getMentions());
        if (!mentions.isEmpty()) comment.setMentions(GSON.toJson(mentions));
        eruptDao.persistAndFlush(comment);
        Long uid = eruptUserService.getCurrentUid();
        // everyone named gets a notice, the author included when they mention themselves
        mentionNotifier.ifAvailable(notifier -> notifier.notify(comment, mentions.stream().map(MentionVo::getId).toList()));
        // re-read so the creator reference is populated for the response
        return CommentVo.of(eruptDao.find(EruptRecordComment.class, comment.getId()), uid);
    }

    // Only ids that name an existing user are kept; names are snapshotted for highlighting.
    private List<MentionVo> resolveMentions(List<Long> ids) {
        if (null == ids || ids.isEmpty()) return List.of();
        Set<Long> unique = new LinkedHashSet<>(ids);
        Map<Long, EruptUserVo> users = eruptDao.lambdaQuery(EruptUserVo.class).in(EruptUserVo::getId, unique)
                .list().stream().collect(Collectors.toMap(EruptUserVo::getId, u -> u));
        return unique.stream().map(users::get).filter(Objects::nonNull)
                .map(u -> new MentionVo(u.getId(), u.getName())).toList();
    }

    // Thread-head flags anyone with access to the model may toggle: resolved collapses the
    // thread, pinned moves it to the top.
    @Transactional
    public CommentVo setResolved(String erupt, String recordId, Long commentId, boolean value) {
        EruptRecordComment head = this.findHead(erupt, recordId, commentId);
        head.setResolved(value);
        return CommentVo.of(eruptDao.mergeAndFlush(head), eruptUserService.getCurrentUid());
    }

    @Transactional
    public CommentVo setPinned(String erupt, String recordId, Long commentId, boolean value) {
        EruptRecordComment head = this.findHead(erupt, recordId, commentId);
        head.setPinned(value);
        return CommentVo.of(eruptDao.mergeAndFlush(head), eruptUserService.getCurrentUid());
    }

    // Only the author (or a super admin) may delete; a deleted thread head takes its replies along.
    @Transactional
    public void delete(String erupt, String recordId, Long commentId) {
        EruptRecordComment comment = this.findComment(erupt, recordId, commentId);
        Long uid = eruptUserService.getCurrentUid();
        boolean mine = null != comment.getCreateUser() && null != uid && uid.equals(comment.getCreateUser().getId());
        if (!mine && !eruptUserService.getSimpleUserInfo().isSuperAdmin()) {
            throw new EruptWebApiRuntimeException("Only the author can delete this comment");
        }
        if (null == comment.getParentId()) {
            eruptDao.lambdaQuery(EruptRecordComment.class)
                    .eq(EruptRecordComment::getParentId, comment.getId())
                    .list().forEach(eruptDao::delete);
        }
        eruptDao.delete(comment);
    }

    private EruptRecordComment findComment(String erupt, String recordId, Long commentId) {
        EruptRecordComment comment = eruptDao.find(EruptRecordComment.class, commentId);
        if (null == comment || !erupt.equals(comment.getErupt()) || !recordId.equals(comment.getRecordId())) {
            throw new EruptWebApiRuntimeException("Comment not found");
        }
        return comment;
    }

    private EruptRecordComment findHead(String erupt, String recordId, Long commentId) {
        EruptRecordComment comment = this.findComment(erupt, recordId, commentId);
        if (null != comment.getParentId()) throw new EruptWebApiRuntimeException("Only a thread head can be flagged");
        return comment;
    }

    // @Power(comment = false) hides the entry in the UI; the API refuses writes as well.
    private void checkEnabled(String erupt) {
        EruptModel model = EruptCoreService.getErupt(erupt);
        if (null != model && !model.getErupt().power().comment()) {
            throw new EruptWebApiRuntimeException("Comments are disabled for " + erupt);
        }
    }
}
