package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import xyz.erupt.core.view.EruptModel;

import java.util.List;

/**
 * Takes over an insert, an update or a delete before it reaches the table, e.g. to send it through an approval
 * (maker-checker) and apply it only once approved. Every Spring bean implementing it is asked in turn;
 * the first one to answer with a message owns the change, and the table is left untouched.
 *
 * @author YuePeng
 * date 2026/11/10
 */
public interface EruptModifyGate {

    /**
     * @return a message for the user when the insert was taken over, null to let it proceed
     */
    default String add(EruptModel eruptModel, JsonObject data) {
        return null;
    }

    /**
     * @return a message for the user when the update was taken over, null to let it proceed
     */
    default String update(EruptModel eruptModel, JsonObject data) {
        return null;
    }

    /**
     * @return a message for the user when the delete was taken over, null to let it proceed
     */
    default String delete(EruptModel eruptModel, List<Object> ids) {
        return null;
    }

}
