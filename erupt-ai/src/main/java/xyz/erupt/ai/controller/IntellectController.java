package xyz.erupt.ai.controller;

import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.ai.util.MarkDownUtil;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/6/4 22:10
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/intellect")
public class IntellectController {

    @Resource
    private LLMService llmService;

    @GetMapping("/form/fill/{erupt}")
    public R<Map<String, Object>> formFill(@PathVariable("erupt") String eruptName,
                                           @RequestParam(required = false) String prompt) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("根据如下key value 映射，随机生成json\n");
        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
            Edit edit = field.getEruptField().edit();
            if (!"".equals(edit.title())) {
                promptBuilder.append(field.getFieldName()).append("=")
                        .append(edit.title()).append("".equals(edit.desc()) ? "" : edit.desc()).append("\n");
            }
        }
        promptBuilder.append("\n\n").append("用户的生成需求是：").append(prompt);
        String result = llmService.send(promptBuilder.toString());
        return R.ok(GsonFactory.getGson().fromJson(MarkDownUtil.extractCodeBlock(result), new TypeToken<Map<String, Object>>() {
        }.getType()));
    }

}
