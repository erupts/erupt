package xyz.erupt.core.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
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
    private final static GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME)
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                    -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME))))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context)
                    -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(DateUtil.DATE))))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext)
                    -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(DateUtil.DATE_TIME)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext)
                    -> LocalDate.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(DateUtil.DATE)))
            .registerTypeAdapter(Long.class, (JsonSerializer<Long>) (src, type, jsonSerializationContext) -> serializeSafeNumber(src))
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, type, jsonSerializationContext) -> serializeDoubleValue(src))
            .registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, type, jsonSerializationContext) -> serializeSafeNumber(src))
            .setObjectToNumberStrategy(new GsonFactory())
            .setExclusionStrategies(new EruptGsonExclusionStrategies());

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
