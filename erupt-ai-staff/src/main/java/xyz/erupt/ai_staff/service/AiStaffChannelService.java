package xyz.erupt.ai_staff.service;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_staff.channel.ChannelMessage;
import xyz.erupt.ai_staff.channel.ChannelRequest;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.ai_staff.model.AiStaffChannel;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * Routes channel callbacks: acks the platform immediately and answers the
 * message asynchronously as the channel's staff.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Service
@Slf4j
public class AiStaffChannelService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiStaffService aiStaffService;

    public String onCallback(String code, ChannelRequest request) {
        AiStaffChannel channel = eruptDao.lambdaQuery(AiStaffChannel.class)
                .eq(AiStaffChannel::getCode, code).eq(AiStaffChannel::getEnable, true).limit(1).one();
        if (null == channel) throw new EruptWebApiRuntimeException("Channel not found: " + code);
        StaffChannel staffChannel = this.channelOf(channel);
        // Platforms require a fast ack; the LLM answer goes out through the async path
        return staffChannel.onCallback(this.configOf(channel), request, message ->
                EruptSpringUtil.getBean(AiStaffChannelService.class).answerAsync(channel.getId(), message));
    }

    @Async
    public void answerAsync(Long channelId, ChannelMessage message) {
        AiStaffChannel channel = eruptDao.find(AiStaffChannel.class, channelId);
        if (null == channel) return;
        if (null == channel.getStaff() || !Boolean.TRUE.equals(channel.getStaff().getEnable())) {
            log.warn("Channel [{}] received a message but has no staff on duty", channel.getName());
            return;
        }
        StaffChannel staffChannel = this.channelOf(channel);
        try {
            staffChannel.reply(this.configOf(channel), message,
                    aiStaffService.chat(channel.getStaff(), message.getContent()));
        } catch (Exception e) {
            log.error("Channel [{}] answer failed", channel.getName(), e);
            try {
                staffChannel.reply(this.configOf(channel), message, "Error: " + e.getMessage());
            } catch (Exception ignore) {
            }
        } finally {
            MetaContext.remove();
        }
    }

    private StaffChannel channelOf(AiStaffChannel channel) {
        StaffChannel staffChannel = StaffChannel.get(channel.getType());
        if (null == staffChannel) throw new EruptWebApiRuntimeException("Unknown channel type: " + channel.getType());
        return staffChannel;
    }

    private JsonObject configOf(AiStaffChannel channel) {
        return GsonFactory.getGson().fromJson(channel.getConfig(), JsonObject.class);
    }

}
