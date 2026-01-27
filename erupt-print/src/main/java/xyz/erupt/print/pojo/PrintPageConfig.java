package xyz.erupt.print.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2026/1/27 22:42
 */
@Getter
@Setter
public class PrintPageConfig {

    private String paperSize;

    private String orientation;

    private Integer marginTop;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

}
