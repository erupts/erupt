package xyz.erupt.bi.cube.pojo.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CubeResultVo {

    private Object value;

    private Object formatValue;

    public CubeResultVo(Object value, Object formatValue) {
        this.value = value;
        this.formatValue = formatValue;
    }

    public CubeResultVo(Object value) {
        this.value = value;
    }
}
