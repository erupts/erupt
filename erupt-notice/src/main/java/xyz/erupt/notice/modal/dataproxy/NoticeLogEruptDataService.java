package xyz.erupt.notice.modal.dataproxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.service.EruptDataServiceDbImpl;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.notice.service.EruptNoticeService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class NoticeLogEruptDataService implements IEruptDataService {

    @Resource
    private EruptDataServiceDbImpl eruptDataServiceDb;

    @Resource
    private EruptNoticeService eruptNoticeService;

    static {
        DataProcessorManager.register(NoticeLog.class.getSimpleName(), NoticeLogEruptDataService.class);
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return eruptDataServiceDb.findDataById(eruptModel, id);
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        return eruptDataServiceDb.queryList(eruptModel, page, eruptQuery);
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return eruptDataServiceDb.queryColumn(eruptModel, columns, eruptQuery);
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        NoticeLog noticeLog = (NoticeLog) object;
        NoticeMessage noticeMessage = new NoticeMessage();
        noticeMessage.setTitle(noticeLog.getTitle());
        noticeMessage.setContent(noticeLog.getContent());
        noticeMessage.setUrl(noticeLog.getUrl());
        eruptNoticeService.send(noticeLog.getNoticeScene(), noticeLog.getChannels().stream().toList(), noticeLog.getReceiveUsers().stream().toList(), noticeMessage);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        eruptDataServiceDb.editData(eruptModel, object);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        eruptDataServiceDb.deleteData(eruptModel, object);
    }

}
