package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author YuePeng
 * date 2021/1/31 20:09
 */
@Component
@Getter
@Setter
public class Platform {

    private String uploadPath;

    private String sessionStrategy;

    private String eruptVersion;

    private int eruptCount; //erupt类数量

    private List<String> eruptModules; //模块列表

}
