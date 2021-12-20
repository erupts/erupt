package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(MetaDataProxy.class)
public class MetaModel extends BaseModel {

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

}
