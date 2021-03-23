package xyz.erupt.mybatis.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.mybatis.annotation.BindMapper;

import java.lang.reflect.ParameterizedType;

/**
 * @author YuePeng
 * date 2020/12/8 20:49
 */
public class EruptMybatisService {

    public static BaseMapper findMapper(Class<?> eruptClass) {
        BindMapper bindMapper = eruptClass.getAnnotation(BindMapper.class);
        if (null != bindMapper) {
            return (BaseMapper<?>) EruptSpringUtil.getApplicationContext().getBean(bindMapper.value());
        }
        throw new RuntimeException("not found BindMapper annotation");
    }

    @SneakyThrows
    public static <T> IPage<T> cratePageInstance(Class<T> clazz) {
        ParameterizedType ptype = (ParameterizedType) Page.class.getGenericSuperclass();
        Class cla = (Class<T>) ptype.getActualTypeArguments()[0];
        return (IPage<T>) cla.newInstance();
    }

}
