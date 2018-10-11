package com.erupt.annotation.sub_field;

import com.erupt.annotation.constant.DataLength;
import com.erupt.annotation.sub_field.sub_edit.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Edit {

    String title() default "";

    boolean notNull() default false;

    EditType type() default EditType.INPUT;

    boolean show() default true;

    boolean readOnly() default false;

    int sort() default 0;

    String group() default "";


    InputType inputType() default @InputType;

    ReferenceType referenceType() default @ReferenceType;

    BoolType boolType() default @BoolType;

    ChoiceType choiceType() default @ChoiceType;

    DictType dictType() default @DictType;

    class Model {
        private String title;

        private EditType type;

        private String placeholder;

        private String defaultVal;

        private boolean show;

        private int sort;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public EditType getType() {
            return type;
        }

        public void setType(EditType type) {
            this.type = type;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getDefaultVal() {
            return defaultVal;
        }

        public void setDefaultVal(String defaultVal) {
            this.defaultVal = defaultVal;
        }

        public boolean isShow() {
            return show;
        }

        public void setShow(boolean show) {
            this.show = show;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }
    }


}
