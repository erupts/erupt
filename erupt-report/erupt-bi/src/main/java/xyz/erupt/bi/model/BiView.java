package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author YuePeng
 * date 2022/7/19 00:17
 */
@Getter
@Setter
//@Entity
@Table(name = "e_bi_view")
@Where(clause = "deleteTime is null")
@SQLDelete(sql = "update e_bi_view set deleteTime = now() where id = ?")
public class BiView extends MetaModelUpdateVo {

    //编码
    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    private String code;

    //名称
    private String name;

    //发布状态
    private Boolean publish = false;

    //编辑时配置
    @Lob
    private String editConfig;

    //已发布配置
    @Lob
    private String viewConfig;

    //删除时间
    private Date deleteTime;

}
