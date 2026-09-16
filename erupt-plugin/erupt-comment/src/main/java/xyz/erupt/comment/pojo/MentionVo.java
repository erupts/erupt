package xyz.erupt.comment.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A user named with @ in a comment, and the candidates offered while typing one.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentionVo {

    private Long id;

    private String name;

    private String avatar;

    public MentionVo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
