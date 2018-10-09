package com.erupt.annotation.sub_field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface View {

    String column() default "";

    String title() default "";

    boolean show() default true;

    int sort() default Integer.MAX_VALUE;


    class Model {

        public Model(View view) {
            this.title = view.title();
            this.show = view.show();
            this.sort = view.sort();
        }

        private String title;

        private boolean show;

        private int sort;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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
