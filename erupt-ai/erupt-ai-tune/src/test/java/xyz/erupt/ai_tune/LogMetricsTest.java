package xyz.erupt.ai_tune;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_tune.core.LogMetrics;
import xyz.erupt.ai_tune.core.TuneEventVo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuePeng
 * date 2026/10/8
 */
public class LogMetricsTest {

    private static TuneEventVo parse(String message) {
        TuneEventVo vo = new TuneEventVo();
        vo.setMessage(message);
        LogMetrics.apply(vo);
        return vo;
    }

    @Test
    public void extractsStepAndLosses() {
        TuneEventVo vo = parse("Step 120/300: training loss=1.2345, valid_loss: 1.5");
        assertEquals(120, vo.getStep());
        assertEquals(1.2345, vo.getTrainLoss(), 1e-9);
        assertEquals(1.5, vo.getValidLoss(), 1e-9);
    }

    @Test
    public void validationLossIsNotMistakenForTrainLoss() {
        TuneEventVo vo = parse("epoch 2 eval_loss 0.9");
        assertNull(vo.getTrainLoss());
        assertEquals(0.9, vo.getValidLoss(), 1e-9);
    }

    @Test
    public void plainMessagesStayUntouched() {
        TuneEventVo vo = parse("Validating training file");
        assertNull(vo.getStep());
        assertNull(vo.getTrainLoss());
        assertNull(vo.getValidLoss());
    }

    @Test
    public void scientificNotationLoss() {
        TuneEventVo vo = parse("step 5 loss 3.2e-2");
        assertEquals(0.032, vo.getTrainLoss(), 1e-9);
    }

}
