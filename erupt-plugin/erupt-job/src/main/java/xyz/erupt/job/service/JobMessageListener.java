package xyz.erupt.job.service;

import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.toolkit.notify.DataAction;
import xyz.erupt.toolkit.notify.NotifyData;
import xyz.erupt.toolkit.notify.RedisNotifier;

import java.text.ParseException;

@Service
public class JobMessageListener {

    public static final String JOB_TOPIC = "erupt-job:data-proxy:notify";

    @Resource
    private RedisNotifier redisNotifier;

    @Resource
    private EruptJobService eruptJobService;

    @PostConstruct
    public void post() {
        redisNotifier.subscribe(JOB_TOPIC, message -> {
            NotifyData<EruptJob> notifyData = GsonFactory.getGson().fromJson(message, new TypeToken<NotifyData<EruptJob>>() {
            }.getType());
            try {
                if (DataAction.ADD.equals(notifyData.getAction())) {
                    eruptJobService.addJob(notifyData.getData());
                } else if (DataAction.UPDATE.equals(notifyData.getAction())) {
                    eruptJobService.modifyJob(notifyData.getData());
                } else if (DataAction.DELETE.equals(notifyData.getAction())) {
                    eruptJobService.deleteJob(notifyData.getData());
                }
            } catch (SchedulerException | ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
