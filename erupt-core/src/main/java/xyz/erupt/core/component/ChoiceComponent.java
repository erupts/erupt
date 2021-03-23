package xyz.erupt.core.component;

import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;

import java.lang.annotation.Annotation;

/**
 * @author liyuepeng
 * @date 2021/3/20 19:40
 */
public class ChoiceComponent implements EruptComponent {

    @Override
    public Class<? extends Annotation> bindAnnotation() {
        return ChoiceType.class;
    }
}
