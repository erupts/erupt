package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OaUsers implements Serializable {
    private static final long serialVersionUID = 771373304182417054L;

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @TableId(type = IdType.AUTO)
    private Long userId;
    /**
    * 用户名
    */
    private String userName;
    /**
    * 拼音  全拼
    */
    private String pingyin;
    /**
    * 拼音, 首字母缩写
    */
    private String py;
    /**
    * 昵称
    */
    private String alisa;
    /**
    * 头像base64
    */
    private String avatar;
    /**
    * 性别
    */
    private Boolean sex;
    /**
    * 入职日期
    */
    private Date entryDate;
    /**
    * 离职日期
    */
    private Date leaveDate;
    /**
    * 创建时间
    */
    private Date created;
    /**
    * 更新时间
    */
    private Date updated;


}
