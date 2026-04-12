package xyz.erupt.notice.model.dataproxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.model.NoticeLog;
import xyz.erupt.notice.model.NoticeLogDetail;

/**
 * @author YuePeng
 * date 2025/12/8 22:48
 */
@Component
public class NoticeLogDataProxy implements DataProxy<NoticeLog> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public void beforeDelete(NoticeLog noticeLog) {
        eruptDao.lambdaQuery(NoticeLogDetail.class).eq(NoticeLogDetail::getNoticeLog, noticeLog).delete();
    }

}
