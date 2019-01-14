package xyz.erupt.core.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

/**
 * Created by liyuepeng on 11/14/18.
 */
@Service
public class GsonService {

    public static final Gson gson = new Gson();

    public static final Gson exposeGson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        exposeGson = builder.create();
    }
}
