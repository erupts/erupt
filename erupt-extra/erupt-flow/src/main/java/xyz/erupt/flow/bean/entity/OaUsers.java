package xyz.erupt.flow.bean.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OaUsers implements Serializable {
    private static final long serialVersionUID = 771373304182417054L;

    @TableId(type = IdType.AUTO)
    /**
    * 用户id
    */
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
