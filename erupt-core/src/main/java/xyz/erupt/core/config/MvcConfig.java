package xyz.erupt.core.config;


import com.google.gson.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import xyz.erupt.annotation.config.SkipSerialize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @author liyuepeng
 * @date 10/31/18.
 */
@Configuration
public class MvcConfig  {
	//序列化
    @Bean
    public HttpMessageConverters customConverters() {
        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
				.setPrettyPrinting()
				/* 更改先后顺序没有影响 */
				.registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
					String datetime = json.getAsJsonPrimitive().getAsString();
					return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				}).registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> {
					String datetime = json.getAsJsonPrimitive().getAsString();
					return LocalDate.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				})
				.setLongSerializationPolicy(LongSerializationPolicy.STRING)
				.create();
        gsonHttpMessageConverter.setGson(gson);
        messageConverters.add(gsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }
    @Bean
    public Gson gson() {
        return new GsonBuilder()
				.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                SkipSerialize skip = f.getAnnotation(SkipSerialize.class);
                return null != skip;
            }

            @Override
            public boolean shouldSkipClass(Class<?> incomingClass) {
                return false;
            }
        }).setDateFormat("yyyy-MM-dd HH:mm:ss")
				.serializeNulls()
				.create();
    }

}
