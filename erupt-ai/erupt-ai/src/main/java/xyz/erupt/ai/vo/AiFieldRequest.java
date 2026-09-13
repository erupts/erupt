package xyz.erupt.ai.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Inline field-assistant request: what the user is writing, and what they want done to it.
 *
 * @author YuePeng
 */
@Getter
@Setter
public class AiFieldRequest {

    public enum Action {
        // Draft the field from scratch, using the rest of the form as the brief
        GENERATE,
        // Rewrite what is already there, keeping the meaning
        POLISH,
        // Carry on from where the text stops
        CONTINUE,
        // Say the same thing in fewer words
        SHORTEN,
        // Say the same thing in more detail
        EXPAND,
        // Follow the user's own instruction verbatim
        CUSTOM
    }

    private Action action = Action.GENERATE;

    // The user's own words; the only input for CUSTOM, an extra hint for every other action
    private String instruction;

    // Current field value, the text the action operates on
    private String current;

    // Sub-range of `current` the user highlighted; when set, only this part is rewritten
    private String selection;

    // Values of the sibling fields on the same form, keyed by field name. The point of the
    // whole feature: the model drafts one field knowing what the rest of the record says.
    private Map<String, Object> form;

}
