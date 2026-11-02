package xyz.erupt.cloud.node.lite;

import java.util.Map;

/** The acting user of a request, as the server forwards it (the only identity a node gets). */
public record User(Long uid, String account, String name, String tenantId) {

    static User of(Object json) {
        if (!(json instanceof Map<?, ?> m)) return null;
        Object uid = Values.normalize(m.get("uid"));
        return new User(uid instanceof Long l ? l : null, text(m.get("account")), text(m.get("name")), text(m.get("tenantId")));
    }

    private static String text(Object value) {
        return value == null ? null : Values.text(value);
    }
}
