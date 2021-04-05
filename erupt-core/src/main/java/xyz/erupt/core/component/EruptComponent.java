package xyz.erupt.core.component;

import java.lang.annotation.Annotation;

/**
 * @author YuePeng
 * date 2021/3/20 19:40
 */
public interface EruptComponent<A extends Annotation> {

    void validate(A annotation, Object value);

}
