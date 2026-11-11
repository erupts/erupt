package xyz.erupt.test.core;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import xyz.erupt.core.service.EruptModifyGate;
import xyz.erupt.core.view.EruptModel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A gate that takes changes over on demand, for {@link EruptControllerTest}. It lives in the one application
 * context every test shares and lets everything through unless a test names the model to take over.
 */
@Component
public class StandInModifyGate implements EruptModifyGate {

    static final String MESSAGE = "taken over";

    // the model whose changes are taken over, null to let every change reach its table
    static volatile String takeOver;

    static final List<String> TAKEN = new CopyOnWriteArrayList<>();

    @Override
    public String add(EruptModel eruptModel, JsonObject data) {
        return this.take(eruptModel, "add");
    }

    @Override
    public String update(EruptModel eruptModel, JsonObject data) {
        return this.take(eruptModel, "update");
    }

    @Override
    public String delete(EruptModel eruptModel, List<Object> ids) {
        return this.take(eruptModel, "delete:" + ids.size());
    }

    private String take(EruptModel eruptModel, String operation) {
        if (!eruptModel.getEruptName().equals(takeOver)) return null;
        TAKEN.add(operation);
        return MESSAGE;
    }

}
