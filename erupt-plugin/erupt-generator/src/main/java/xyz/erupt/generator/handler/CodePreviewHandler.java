package xyz.erupt.generator.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.generator.service.CodeRender;

import java.util.List;

/**
 * Reads one generated class in the code editor the admin uses everywhere else.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Component
public class CodePreviewHandler implements OperationHandler<GeneratorClass, Void> {

    @Override
    public String exec(List<GeneratorClass> data, Void unused, String[] param) {
        GeneratorClass clazz = data.get(0);
        return "codeDrawer('java', " + GsonFactory.getGson().toJson(CodeRender.render(clazz))
                + ", " + GsonFactory.getGson().toJson(CodeFiles.name(clazz)) + ")";
    }

}
