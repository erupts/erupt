package xyz.erupt.annotation.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2021/11/11 22:41
 */
@Getter
@Setter
@Builder
public class Row {

    private List<Column> columns;

    private String className;

}
