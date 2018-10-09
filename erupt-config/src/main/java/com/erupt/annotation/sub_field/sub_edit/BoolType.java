package com.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 10/9/18.
 */
public @interface BoolType {
    String trueText() default "";

    String falseText() default "";

    boolean defaultValue() default false;


    class Model {
        private String trueText;

        private String falseText;

        private boolean defaultValue;

        public Model(BoolType boolType) {
            this.trueText = boolType.trueText();
            this.falseText = boolType.falseText();
            this.defaultValue = boolType.defaultValue();
        }
    }
}
