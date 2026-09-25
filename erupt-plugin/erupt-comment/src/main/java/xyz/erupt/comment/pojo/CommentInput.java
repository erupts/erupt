package xyz.erupt.comment.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommentInput {

    private String content;

    // top-level comment this one answers; null for a new thread
    private Long parentId;

    // ids of the users named with @ in the content
    private List<Long> mentions;

}
