package xyz.erupt.security.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/4/6 16:46
 */
@Getter
@Setter
public class ReqBody {

    private long date;

    private Object body;

    public ReqBody() {
    }
}
