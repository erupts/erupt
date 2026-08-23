package xyz.erupt.ai_staff.proxy;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.ai_staff.model.AiStaffChannel;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

/**
 * In-form "Test Connect" button: verifies the channel credentials against the
 * unsaved form values, so a config can be checked before it is submitted.
 *
 * @author YuePeng
 * date 2026/8/19
 */
@Service
public class AiStaffChannelTestButtonHandler implements EruptButtonHandler<AiStaffChannel> {

    @Override
    public String click(AiStaffChannel channel, String[] params) {
        StaffChannel staffChannel = StaffChannel.get(channel.getType());
        if (null == staffChannel) {
            throw new EruptWebApiRuntimeException("Unknown channel type: " + channel.getType());
        }
        JsonObject config;
        try {
            config = GsonFactory.getGson().fromJson(channel.getConfig(), JsonObject.class);
        } catch (Exception e) {
            config = null;
        }
        if (null == config) {
            throw new EruptWebApiRuntimeException("Channel config must be a valid JSON object");
        }
        if (!staffChannel.testConnect(config)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("No verifiable credentials configured"));
        }
        return "alert(" + GsonFactory.getGson().toJson(I18nTranslate.$translate("Credentials verified")) + ")";
    }

}
