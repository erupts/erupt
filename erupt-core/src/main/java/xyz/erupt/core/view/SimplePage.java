package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/11/25 23:05
 */
@Getter
@Setter
public class SimplePage<T> {

    private Long total;

    private List<T> list;

}
