package xyz.erupt.core.simple;

import lombok.Getter;
import xyz.erupt.annotation.EruptField;

import java.io.Serializable;

/**
 * @author YuePeng
 * date 2022/5/5 21:57
 */
@Getter
public class SimpleErupt {

    @EruptField
    private Serializable id;

}
