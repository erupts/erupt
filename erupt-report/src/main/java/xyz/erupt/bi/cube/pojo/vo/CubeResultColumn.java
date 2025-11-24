package xyz.erupt.bi.cube.pojo.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CubeResultColumn {

    private Object value;

    private Object formatValue;

    public CubeResultColumn(Object value, Object formatValue) {
        this.value = value;
        this.formatValue = formatValue;
    }

    public CubeResultColumn(Object value) {
        this.value = value;
    }
}
