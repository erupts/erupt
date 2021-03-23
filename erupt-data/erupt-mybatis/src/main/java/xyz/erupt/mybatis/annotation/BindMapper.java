package xyz.erupt.mybatis.annotation;

import com.baomidou.mybatisplus.core.mapper.Mapper;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2021/3/12 16:38
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface BindMapper {

    Class<? extends Mapper<?>> value();
}
