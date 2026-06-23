package xyz.erupt.notice.cube;

import xyz.erupt.annotation.cube.Dimension;
import xyz.erupt.annotation.cube.EruptCube;
import xyz.erupt.annotation.cube.Measure;

import java.util.Date;

/**
 * @author YuePeng
 * date 2026/4/11 23:45
 */
@EruptCube(
        name = "Erupt Notice Log",
        sql = """
                select detail.status,
                     detail.success,
                     detail.create_time,
                     detail.channel,
                     detail.receive_user_id,
                     log.title,
                     scene.name
                from e_notice_log_detail detail
                       inner join e_notice_log log on detail.notice_log_id = log.id
                       inner join e_notice_scene scene on log.notice_scene_id = scene.id
                """
)
public class EruptNoticeLogCube {

    @Dimension(title = "Scene", sql = "name")
    private String name;

    @Dimension(title = "Channel", sql = "channel")
    private String channel;

    @Dimension(title = "Status", sql = "status")
    private String status;

    @Dimension(title = "Is Success", sql = "success")
    private Boolean success;

    @Dimension(title = "Notice Title", sql = "title")
    private String title;

    @Dimension(title = "Receive User", sql = "receive_user_id")
    private Long receiveUserId;

    @Dimension(title = "Create Time", sql = "create_time")
    private Date createTime;

    @Measure(title = "Count", sql = "count(*)")
    private Long count;

    @Measure(title = "Success Count", sql = "sum(case when success then 1 else 0 end)")
    private Long successCount;

    @Measure(title = "Fail Count", sql = "sum(case when not success then 1 else 0 end)")
    private Long failCount;

    @Measure(title = "Unique Receivers", sql = "count(distinct receive_user_id)")
    private Long uniqueReceivers;

}
