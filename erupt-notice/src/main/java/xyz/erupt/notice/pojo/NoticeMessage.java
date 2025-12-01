package xyz.erupt.notice.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NoticeMessage {

    private String title;

    private String content;

    private String url;

    private Map<String, Object> params;

}
