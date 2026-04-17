package xyz.erupt.test.model.edit;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.util.List;

@Component
public class TestTagsFetchHandler implements TagsFetchHandler {

    @Override
    public List<String> fetchTags(String[] params) {
        return List.of("Spring", "Erupt", "JPA", "Redis", "Kafka", "Docker");
    }
}
