package xyz.erupt.core.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import xyz.erupt.annotation.model.Location;
import xyz.erupt.core.util.DateUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author YuePeng
 * date 2021/3/1 13:03
 */
@NoArgsConstructor
public class GsonFactory implements ToNumberStrategy {

    public static final double JS_MAX_NUMBER = 9007199254740991.0;

    public static final double JS_MIN_NUMBER = -9007199254740991.0;

    @Getter
    private final static GsonBuilder gsonBuilder = new GsonBuilder()
            .setDateFormat(DateUtil.ISO_8601)
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                    -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(src.toString().length() == 10 ? DateUtil.DATE : DateUtil.ISO_8601))))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context)
                    -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(DateUtil.DATE))))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext)
                    -> DateUtil.parseLocalDateTime(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext)
                    -> DateUtil.parseLocalDate(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(Long.class, (JsonSerializer<Long>) (src, type, jsonSerializationContext) -> serializeSafeNumber(src))
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, type, jsonSerializationContext) -> serializeDoubleValue(src))
            .registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, type, jsonSerializationContext) -> serializeSafeNumber(src))
            .registerTypeAdapter(Location.class, (JsonDeserializer<Location>) (json, type, context) -> parseLocation(json))
            .setObjectToNumberStrategy(new GsonFactory())
//            .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, type, ctx) -> {
//                Instant instant = src.toInstant();
//                return new JsonPrimitive(
//                        DateTimeFormatter.ISO_INSTANT.format(instant)); // 2023-12-13T06:30:45.123Z
//            })
            .setExclusionStrategies(new EruptGsonExclusionStrategies());

    /**
     * A MAP field arrives from the editor as the JSON text it stores, and from an API caller
     * as an object; both become a Location. Older rows carry AMap's raw tip shape, whose
     * coordinates sit under {@code location}.
     */
    private static Location parseLocation(JsonElement json) {
        if (null == json || json.isJsonNull()) return null;
        if (json.isJsonPrimitive()) {
            String text = json.getAsString();
            if (text.isBlank()) return null;
            json = JsonParser.parseString(text);
        }
        if (!json.isJsonObject()) return null;
        JsonObject o = json.getAsJsonObject();
        JsonObject coords = o.has("location") && o.get("location").isJsonObject() ? o.getAsJsonObject("location") : o;
        Location location = new Location(number(coords, "lng"), number(coords, "lat"), string(o, "name"), string(o, "address"));
        if (o.has("crs") && !o.get("crs").isJsonNull()) location.setCrs(Location.Crs.valueOf(o.get("crs").getAsString()));
        return location;
    }

    private static Double number(JsonObject o, String key) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsDouble() : null;
    }

    private static String string(JsonObject o, String key) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
    }

    private static JsonPrimitive serializeSafeNumber(Number src) {
        if (src.doubleValue() > JS_MAX_NUMBER || src.doubleValue() < JS_MIN_NUMBER) {
            return new JsonPrimitive(src.toString());
        } else {
            return new JsonPrimitive(src);
        }
    }

    private static JsonPrimitive serializeDoubleValue(Double src) {
        if (src > JS_MAX_NUMBER || src < JS_MIN_NUMBER) {
            return new JsonPrimitive(new BigDecimal(src).toPlainString());
        }
        if (Math.abs(src - src.longValue()) < Math.ulp(src)) {
            return new JsonPrimitive(src.longValue());
        } else {
            return new JsonPrimitive(String.format("%.15g", src).replaceAll("\\.?0+$", ""));
        }
    }

    @Getter
    private static final Gson gson = gsonBuilder.serializeNulls().create();


    @Override
    public Number readNumber(JsonReader in) throws IOException {
        String value = in.nextString();
        if (NumberUtils.isCreatable(value)) {
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException var6) {
            try {
                Double d = Double.valueOf(value);
                if ((d.isInfinite() || d.isNaN()) && !in.isLenient()) {
                    throw new MalformedJsonException("JSON forbids NaN and infinities: " + d + "; at path " + in.getPreviousPath());
                } else {
                    return d;
                }
            } catch (NumberFormatException e) {
                throw new JsonParseException("Cannot parse " + value + "; at path " + in.getPreviousPath(), e);
            }
        }
    }
}
