package xyz.erupt.server.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

/**
 * @author liyuepeng
 * @date 2021/1/23 23:26
 */
@Getter
@Setter
public class Sys {

    private String name; //系统名称

    private String arch; //架构

    private String userDir;

    Sys() {
        Properties props = System.getProperties();
        this.setName(props.getProperty("os.name"));
        this.setArch(props.getProperty("os.arch"));
        this.setUserDir(props.getProperty("user.dir"));
    }
}
