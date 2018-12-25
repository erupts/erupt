package xyz.erupt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * Created by liyuepeng on 10/31/18.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gsonHttpMessageConverter.setGson(gson);
        converters.add(gsonHttpMessageConverter);
        super.configureMessageConverters(converters);
    }



//    @Bean
//    public HttpMessageConverters customConverters() {
//        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
//        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
//        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
//        gsonHttpMessageConverter.setGson(gson);
//        messageConverters.add(gsonHttpMessageConverter);
//        return new HttpMessageConverters(true, messageConverters);
//    }

}
