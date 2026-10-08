package xyz.erupt.ai_tune.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls step and loss figures out of free-text training log lines, for providers that
 * stream plain logs instead of structured metric events.
 *
 * @author YuePeng
 * date 2026/10/8
 */
public final class LogMetrics {

    private LogMetrics() {
    }

    private static final Pattern STEP = Pattern.compile("(?i)\\bstep[\\s:=/]*(\\d+)");

    private static final Pattern TRAIN_LOSS = Pattern.compile("(?i)\\b(?:train(?:ing)?[_ ]?loss|loss)\\b[\\s:=]*([0-9]*\\.?[0-9]+(?:e-?\\d+)?)");

    private static final Pattern VALID_LOSS = Pattern.compile("(?i)\\b(?:valid(?:ation)?|eval)[_ ]?loss\\b[\\s:=]*([0-9]*\\.?[0-9]+(?:e-?\\d+)?)");

    /** Fills step, trainLoss and validLoss on the event when the message carries them */
    public static void apply(TuneEventVo event) {
        String text = event.getMessage();
        if (null == text) return;
        if (null == event.getStep()) {
            Matcher m = STEP.matcher(text);
            if (m.find()) event.setStep(Integer.parseInt(m.group(1)));
        }
        if (null == event.getValidLoss()) {
            Matcher m = VALID_LOSS.matcher(text);
            if (m.find()) event.setValidLoss(Double.parseDouble(m.group(1)));
        }
        if (null == event.getTrainLoss()) {
            Matcher m = TRAIN_LOSS.matcher(text);
            // Skip a match that is really the validation loss
            while (m.find()) {
                int start = m.start();
                String prefix = text.substring(Math.max(0, start - 12), start).toLowerCase();
                if (prefix.contains("valid") || prefix.contains("eval")) continue;
                event.setTrainLoss(Double.parseDouble(m.group(1)));
                break;
            }
        }
    }

}
