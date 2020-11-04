package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.query.Condition;

import java.util.List;

@Getter
@Setter
public class TableQueryVo {
    private Integer pageIndex;

    private Integer pageSize;

    private String sort;

    private Object linkTreeVal;

    private List<Condition> condition;
}
