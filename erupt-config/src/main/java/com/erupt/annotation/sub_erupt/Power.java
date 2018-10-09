package com.erupt.annotation.sub_erupt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Power {
    boolean add() default true;

    boolean del() default true;

    boolean edit() default true;

    boolean query() default true;

    boolean export() default true;

    class Model {

        public Model(Power power) {
            add = power.add();
            del = power.del();
            edit = power.edit();
            query = power.query();
            export = power.export();
        }

        private boolean add;

        private boolean del;

        private boolean edit;

        private boolean query;

        private boolean export;

        public boolean isAdd() {
            return add;
        }

        public void setAdd(boolean add) {
            this.add = add;
        }

        public boolean isDel() {
            return del;
        }

        public void setDel(boolean del) {
            this.del = del;
        }

        public boolean isEdit() {
            return edit;
        }

        public void setEdit(boolean edit) {
            this.edit = edit;
        }

        public boolean isQuery() {
            return query;
        }

        public void setQuery(boolean query) {
            this.query = query;
        }

        public boolean isExport() {
            return export;
        }

        public void setExport(boolean export) {
            this.export = export;
        }
    }
}
