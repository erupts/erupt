package xyz.erupt.auth.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-12-10.
 */
@Entity
@Table(name = "E_ILLEGAL_REQ")
@Erupt(
        power = @Power(edit = false, viewDetails = false, delete = false, export = true),
        name = "非法请求检测日志",
        orderBy = "createTime desc"
)
public class EruptIllegalReq extends BaseModel {

    private String ip;

    private String name;

    private String url;

    private Date createTime;

    @ManyToOne
    private EruptUser eruptUser;

}
