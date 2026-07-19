package xyz.erupt.test.model.edit;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

@Component
public class TestChoiceFetchHandler implements ChoiceFetchHandler<Void> {

    @Override
    public List<VLModel> fetch(String[] params) {
        return List.of(
                new VLModel("TECH", "Technology"),
                new VLModel("FINANCE", "Finance"),
                new VLModel("HEALTH", "Health"),
                new VLModel("EDUCATION", "Education")
        );
    }
}
