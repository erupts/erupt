package xyz.erupt.print.service;

import jakarta.annotation.Resource;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.print.var.PrintVar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EruptPrintService {

    public String repeatVar(String content, Map<String, Object> vars) {
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        Map<String, Object> globalVars = new LinkedHashMap<>();
        for (PrintVar value : PrintVar.PRINT_VAR_MAP.values()) {
            globalVars.put(value.code(), value.value());
        }
        globalVars.putAll(vars);
        while (matcher.find()) {
            Object value = globalVars.get(matcher.group(1));
            matcher.appendReplacement(result, value != null ? Matcher.quoteReplacement(value.toString()) : matcher.group(1));
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
