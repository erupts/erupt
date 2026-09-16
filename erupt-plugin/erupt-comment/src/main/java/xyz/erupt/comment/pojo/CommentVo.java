package xyz.erupt.comment.pojo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.comment.model.EruptRecordComment;

import java.util.Date;
import java.util.List;

/**
 * Comment as sent to the record panel: author fields flattened, plus whether the current
 * user may delete it.
 */
@Getter
@Setter
public class CommentVo {

    private static final Gson GSON = new Gson();

    private Long id;

    private Long parentId;

    private String content;

    private Date createTime;

    private Long userId;

    private String userName;

    private String userAvatar;

    private boolean mine;

    private List<MentionVo> mentions;

    private boolean resolved;

    private boolean pinned;

    public static CommentVo of(EruptRecordComment comment, Long currentUid) {
        CommentVo vo = new CommentVo();
        vo.setId(comment.getId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        vo.setResolved(Boolean.TRUE.equals(comment.getResolved()));
        vo.setPinned(Boolean.TRUE.equals(comment.getPinned()));
        if (null != comment.getMentions()) {
            vo.setMentions(GSON.fromJson(comment.getMentions(), new TypeToken<List<MentionVo>>() {
            }.getType()));
        }
        if (null != comment.getCreateUser()) {
            vo.setUserId(comment.getCreateUser().getId());
            vo.setUserName(comment.getCreateUser().getName());
            vo.setUserAvatar(comment.getCreateUser().getAvatar());
            vo.setMine(null != currentUid && currentUid.equals(comment.getCreateUser().getId()));
        }
        return vo;
    }
}
