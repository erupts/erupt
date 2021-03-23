package xyz.erupt.mybatis;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.mybatis.annotation.BindMapper;
import xyz.erupt.mybatis.annotation.BindXmlMapper;

/**
 * @author YuePeng
 * date 2021/3/11 15:09
 */
@Getter
@Setter
@Erupt(name = "mybatis测试", orderBy = "sort")
@TableName(value = "test_employee")
@EruptDataProcessor(EruptMybatisConst.MYBATIS_PROCESS)
@BindMapper(TestModelMapper.class)
@BindXmlMapper("/xml")
public class TestModel {

    @TableId(value = "id", type = IdType.AUTO)//指定自增策略
    @EruptField
    private Integer id;

    //若没有开启驼峰命名，或者表中列名不符合驼峰规则，可通过该注解指定数据库表中的列名，exist标明数据表中有没有对应列
    @TableField(value = "last_name", exist = true)
    @EruptField
    private String lastName;

    @EruptField(
            views = @View(title = "邮箱"),
            edit = @Edit(title = "邮箱")
    )
    private String email;

    @EruptField(
            views = @View(title = "性别"),
            edit = @Edit(title = "性别")
    )
    private Integer gender;

    @EruptField(
            views = @View(title = "年龄"),
            edit = @Edit(title = "年龄")
    )
    private Integer age;
}
