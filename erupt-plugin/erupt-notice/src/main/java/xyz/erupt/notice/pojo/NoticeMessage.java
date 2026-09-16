package xyz.erupt.notice.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class NoticeMessage {

    private Long id;

    /**
     * Id of the NoticeLogDetail row belonging to the recipient currently being served. Filled in
     * by EruptNoticeService right before each channel send, because {@link #id} identifies the
     * shared log entry and cannot address one recipient's copy of it.
     */
    private Long logDetailId;

    private String title;

    private String content;

    private String url;

//    private NoticeUrlOpenWay urlOpenWay = NoticeUrlOpenWay.DRAWER;

    private Map<String, Object> params = new HashMap<>();

}
