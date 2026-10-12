package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.query.Condition;

import java.util.List;

@Getter
@Setter
public class TableQuery extends Page {

    /** Selected @LinkTree node ids that filter the table */
    private List<Object> linkTreeVal;

    /** Search conditions */
    private List<Condition> condition;

    /** Code of the @Vis view being queried, empty for the plain table */
    private String vis;

}
