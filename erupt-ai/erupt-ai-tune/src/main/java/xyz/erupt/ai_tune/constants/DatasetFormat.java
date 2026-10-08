package xyz.erupt.ai_tune.constants;

/**
 * Wire format of one training sample (one JSONL line).
 *
 * @author YuePeng
 * date 2026/10/8
 */
public final class DatasetFormat {

    private DatasetFormat() {
    }

    // Supervised fine-tuning: {"messages":[{"role":"system|user|assistant|tool","content":"..."}]}
    public static final String CHAT = "CHAT";

    // Preference tuning (DPO): {"input":{"messages":[...]},"preferred_output":[...],"non_preferred_output":[...]}
    public static final String PREFERENCE = "PREFERENCE";

}
