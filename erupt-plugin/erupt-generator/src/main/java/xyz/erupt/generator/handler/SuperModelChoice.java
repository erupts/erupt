package xyz.erupt.generator.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.generator.base.SuperModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Offers the parent classes an entity may inherit, named after the java classes themselves.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Component
public class SuperModelChoice implements ChoiceFetchHandler<Void> {

    @Override
    public List<VLModel> fetch(String[] params) {
        return Arrays.stream(SuperModel.values())
                .map(it -> new VLModel(it.name(), it.label(), it.getPackageName()))
                .collect(Collectors.toList());
    }

}
