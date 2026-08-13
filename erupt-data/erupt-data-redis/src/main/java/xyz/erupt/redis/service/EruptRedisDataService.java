package xyz.erupt.redis.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.redis.annotation.EruptRedis;

import java.util.*;

/**
 * Redis data source: each row is one hash stored at {@code <prefix><primary key>},
 * model fields map to hash fields as strings (dates use the erupt Gson format).
 * Rows are enumerated with SCAN on the prefix, then the base class
 * evaluates filtering, sorting and paging — suited to config / dictionary scale
 * keyspaces rather than large datasets (enumeration stops at 10000 keys).
 *
 * @author YuePeng
 */
@Service
public class EruptRedisDataService extends EruptBeanDataService<Object> {

    public static final String DATA_PROCESSOR = "REDIS";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptRedisDataService.class);
    }

    // Guards against unbounded enumeration of a huge keyspace
    private static final int MAX_FETCH_SIZE = 10000;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected List<Object> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptRedis eruptRedis = this.eruptRedis(eruptModel);
        List<Object> beans = new ArrayList<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(
                ScanOptions.scanOptions().match(eruptRedis.value() + "*").count(500).build())) {
            while (cursor.hasNext() && beans.size() < MAX_FETCH_SIZE) {
                Optional.ofNullable(this.readBean(eruptModel, eruptRedis, cursor.next())).ifPresent(beans::add);
            }
        }
        return beans;
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EruptRedis eruptRedis = this.eruptRedis(eruptModel);
        return this.readBean(eruptModel, eruptRedis, eruptRedis.value() + id);
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        String key = this.key(eruptModel, object);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("redis.key_exists") + " → " + key);
        }
        stringRedisTemplate.opsForHash().putAll(key, this.toHash(object));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        String key = this.key(eruptModel, object);
        // rewrite the whole hash so fields cleared in the form do not linger
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForHash().putAll(key, this.toHash(object));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        stringRedisTemplate.delete(this.key(eruptModel, object));
    }

    private Object readBean(EruptModel eruptModel, EruptRedis eruptRedis, String key) {
        Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(key);
        if (hash.isEmpty()) return null;
        JsonObject json = new JsonObject();
        hash.forEach((k, v) -> json.addProperty(String.valueOf(k), String.valueOf(v)));
        // the key suffix is the authoritative primary key value
        json.addProperty(eruptModel.getErupt().primaryKeyCol(), key.substring(eruptRedis.value().length()));
        return GsonFactory.getGson().fromJson(json, eruptModel.getClazz());
    }

    private Map<String, String> toHash(Object object) {
        JsonObject json = GsonFactory.getGson().toJsonTree(object).getAsJsonObject();
        Map<String, String> hash = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonNull()) continue;
            hash.put(entry.getKey(), entry.getValue().isJsonPrimitive()
                    ? entry.getValue().getAsString() : entry.getValue().toString());
        }
        return hash;
    }

    private String key(EruptModel eruptModel, Object object) {
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("redis.primary_key_missing"));
        return this.eruptRedis(eruptModel).value() + id;
    }

    private EruptRedis eruptRedis(EruptModel eruptModel) {
        EruptRedis eruptRedis = eruptModel.getClazz().getAnnotation(EruptRedis.class);
        if (null == eruptRedis) {
            throw new EruptWebApiRuntimeException("@EruptRedis annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptRedis;
    }

}
