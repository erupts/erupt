package xyz.erupt.ai_tune.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai_tune.model.TuneJob;
import xyz.erupt.ai_tune.service.TuneJobService;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.List;

/**
 * Row operations of a job, dispatched on the annotation parameter.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class TuneJobOperationHandler implements OperationHandler<TuneJob, Void> {

    public static final String START = "start";

    public static final String CANCEL = "cancel";

    public static final String SYNC = "sync";

    public static final String REGISTER = "register";

    @Resource
    private TuneJobService tuneJobService;

    @Override
    public String exec(List<TuneJob> data, Void unused, String[] param) {
        switch (param[0]) {
            case START -> {
                tuneJobService.start(data.get(0).getId());
                return "msg.success('" + I18nTranslate.$translate("tune.started") + "')";
            }
            case CANCEL -> tuneJobService.cancel(data.get(0).getId());
            case SYNC -> data.forEach(job -> tuneJobService.sync(job.getId()));
            case REGISTER -> {
                LLM llm = tuneJobService.register(data.get(0).getId());
                return "msg.success('" + I18nTranslate.$translate("tune.registered") + " " + llm.getName().replace("'", "\\'") + "')";
            }
            default -> throw new IllegalArgumentException("Unknown operation: " + param[0]);
        }
        return null;
    }

}
