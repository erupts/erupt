package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2021/1/31 20:09
 */
@Component
@Getter
@Setter
public class Platform {

    private String uploadPath;

    private Boolean redisSession;

}
