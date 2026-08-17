package xyz.erupt.ai_staff.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.ai_staff.model.AiStaffChannel;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.List;
import java.util.Map;

/**
 * Validates channel config, pre-fills the config template on type change,
 * and backs the "Test Push" row operation.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Service
public class AiStaffChannelProxy implements DataProxy<AiStaffChannel>, OnChange<AiStaffChannel>, OperationHandler<AiStaffChannel, Void> {

    private static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void addBehavior(AiStaffChannel channel) {
        channel.setCode(Erupts.generateCode());
    }

    @Override
    public void beforeAdd(AiStaffChannel channel) {
        if (null == StaffChannel.get(channel.getType())) {
            throw new EruptWebApiRuntimeException("Unknown channel type: " + channel.getType());
        }
        try {
            GsonFactory.getGson().fromJson(channel.getConfig(), JsonObject.class);
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException("Channel config must be a valid JSON object");
        }
    }

    @Override
    public void beforeUpdate(AiStaffChannel channel) {
        this.beforeAdd(channel);
    }

    @Override
    public Map<String, Object> populateForm(AiStaffChannel channel, String[] params) {
        if (StringUtils.isBlank(channel.getType())) return Map.of();
        return Map.of(LambdaSee.field(AiStaffChannel::getConfig),
                prettyGson.toJson(StaffChannel.get(channel.getType()).configTemplate()));
    }

    @Override
    public Map<String, String> buildEditExpr(AiStaffChannel channel, String[] params) {
        return Map.of();
    }

    @Override
    public String exec(List<AiStaffChannel> data, Void unused, String[] param) {
        for (AiStaffChannel channel : data) {
            StaffChannel.get(channel.getType()).push(
                    GsonFactory.getGson().fromJson(channel.getConfig(), JsonObject.class),
                    I18nTranslate.$translate("This is a test message from Erupt AI Staff"));
        }
        return "alert(" + GsonFactory.getGson().toJson(I18nTranslate.$translate("Test message sent")) + ")";
    }

}
