package xyz.erupt.ai_staff.channel;

import lombok.Builder;
import lombok.Getter;

import java.util.Locale;
import java.util.Map;

/**
 * Platform callback request normalized off the servlet API, so channel
 * implementations stay plain and testable.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Getter
@Builder
public class ChannelRequest {

    private String method;

    // Header names are stored lower-case
    private Map<String, String> headers;

    private Map<String, String> params;

    private String body;

    public String header(String name) {
        return headers.get(name.toLowerCase(Locale.ROOT));
    }

    public String param(String name) {
        return params.get(name);
    }

}
