package xyz.erupt.mybatis.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperRegistry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itranswarp.compiler.JavaStringCompiler;
import lombok.SneakyThrows;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import xyz.erupt.mybatis.TestModel;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2021/3/11 15:18
 */
@Service
public class AutoRegisterMapper {

    public static final String MAPPER = "Mapper";

    protected final MybatisMapperRegistry mybatisMapperRegistry = new MybatisMapperRegistry(new MybatisConfiguration());

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    //动态创建 mapper 接口 ，效率太低废除此方案
    @SneakyThrows
    public Class<?> compileMapper(Class<?> entity) {
        JavaStringCompiler compiler = new JavaStringCompiler();
        String mapperName = entity.getSimpleName() + "$$" + MAPPER;
        String name = entity.getPackage().getName() + "." + mapperName;
        return compiler.loadClass(name, compiler.compile(mapperName + ".java",
                "package " + entity.getPackage().getName() + ";\n" +
                        "import com.baomidou.mybatisplus.core.mapper.BaseMapper; \n" +
                        "import " + entity.getName() + ";\n" +
                        "public interface " + mapperName + " extends BaseMapper<" + entity.getSimpleName() + "> {}"));
    }

    public void registerMapper() {
        Class<?> mapper = this.compileMapper(TestModel.class);
        mybatisMapperRegistry.addMapper(mapper);
        BaseMapper<?> baseMapper = (BaseMapper<?>) mybatisMapperRegistry.getMapper(mapper, sqlSessionFactory.openSession());
        System.out.println(baseMapper.selectCount(null));
    }


}
