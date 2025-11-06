package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.query.Condition;

import java.util.List;

@Getter
@Setter
public class TableQuery extends Page {

    private List<Object> linkTreeVal;

    private List<Condition> condition;

    // ref @Viz.code
    private String viz;

}
