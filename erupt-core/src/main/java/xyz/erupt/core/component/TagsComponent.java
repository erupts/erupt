package xyz.erupt.core.component;

import xyz.erupt.annotation.sub_field.sub_edit.TagsType;

import java.lang.annotation.Annotation;

/**
 * @author YuePeng
 * date 2021/3/20 19:40
 */
public class TagsComponent implements EruptComponent {

    @Override
    public Class<? extends Annotation> bindAnnotation() {
        return TagsType.class;
    }

}
