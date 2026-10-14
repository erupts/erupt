package xyz.erupt.core.view;

import lombok.Getter;
import java.util.List;
import lombok.Setter;
import xyz.erupt.annotation.fun.PowerObject;

import java.util.Map;

/**
 * @author YuePeng
 * date 2018-09-29.
 */
@Getter
@Setter
public class EruptBuildModel {

    /** Model metadata: fields with their edit and view configuration */
    private EruptModel eruptModel;

    /** Build models of TAB_* and MULTI_FORM fields keyed by field name */
    private Map<String, EruptBuildModel> tabErupts;

    /** Models of COMBINE fields keyed by field name */
    private Map<String, EruptModel> combineErupts;

    /** Models of row operations that open a form, keyed by operation code */
    private Map<String, EruptModel> operationErupts;

    /** What the current user may do on this erupt */
    private PowerObject power;

    /** The scope the model lives in, when it declares one */
    private Scope scope;

    @Getter
    @Setter
    public static class Scope {

        /** Simple class name of the scope form */
        private String name;

        /** Model fields the scope filters and fills; the page hides them from search */
        private List<String> bind;

    }

    @Getter
    @Setter
    public static class Ext {

        private String field;

        private EruptModel eruptModel;

    }

}
