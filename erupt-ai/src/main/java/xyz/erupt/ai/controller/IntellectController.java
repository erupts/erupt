package xyz.erupt.ai.controller;

import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.ai.util.ClassTemplateUtil;
import xyz.erupt.ai.util.MarkDownUtil;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.service.EruptCoreService;
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
        String promptBuilder = "根据如下面的class类结构，生成内容随机的json，注意只返回纯json\n" +
                ClassTemplateUtil.geneEruptFormClass(eruptModel.getClazz());
        String result = llmService.send(promptBuilder);
        return R.ok(GsonFactory.getGson().fromJson(MarkDownUtil.extractCodeBlock(result), new TypeToken<Map<String, Object>>() {
        }.getType()));
    }

}
