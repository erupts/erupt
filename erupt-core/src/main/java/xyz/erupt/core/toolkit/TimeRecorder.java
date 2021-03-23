package xyz.erupt.core.toolkit;

/**
 * @author YuePeng
 * date 2021/3/16 18:32
 */
public class TimeRecorder {

    private Long current;

    public TimeRecorder() {
        this.current = System.currentTimeMillis();
    }

    public long recorder() {
        try {
            return System.currentTimeMillis() - this.current;
        } finally {
            this.current = System.currentTimeMillis();
        }
    }

}
