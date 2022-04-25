package xyz.erupt.mybatis.annotation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2022/4/25 00:27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EruptMybatisORM {

    Class<? extends BaseMapper<?>> mapper();

}
