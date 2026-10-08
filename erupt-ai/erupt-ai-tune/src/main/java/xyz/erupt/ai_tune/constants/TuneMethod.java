package xyz.erupt.ai_tune.constants;

/**
 * @author YuePeng
 * date 2026/10/8
 */
public final class TuneMethod {

    private TuneMethod() {
    }

    // Supervised fine-tuning on CHAT datasets
    public static final String SFT = "SFT";

    // Direct preference optimisation on PREFERENCE datasets
    public static final String DPO = "DPO";

}
