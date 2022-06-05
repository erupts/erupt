package xyz.erupt.monitor.vo.redis;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/1/31 21:47
 */
@Getter
@Setter
public class RedisCmdStat {
    private String name;

    private String value;

    public RedisCmdStat(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
