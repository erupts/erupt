package xyz.erupt.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.SneakyThrows;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.mybatis.service.EruptMybatisService;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020/12/8 20:49
 */
@Service
public class EruptMybatisImpl implements IEruptDataService, ApplicationRunner {

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        BaseMapper baseMapper = EruptMybatisService.findMapper(eruptModel.getClazz());
        return baseMapper.selectById((Serializable) id);
    }

    @Override
    @SneakyThrows
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        BaseMapper baseMapper = EruptMybatisService.findMapper(eruptModel.getClazz());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page.class.newInstance();
        IPage<?> p = EruptMybatisService.cratePageInstance(eruptModel.getClazz());
        p.setSize(page.getPageSize());
        p.setCurrent(page.getPageIndex());
        //TODO build wrapper
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        page.setList(baseMapper.selectMapsPage(p, wrapper).getRecords());
        page.setTotal(baseMapper.selectCount(wrapper).longValue());
        return page;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        BaseMapper baseMapper = EruptMybatisService.findMapper(eruptModel.getClazz());
//        baseMapper.selectb
        return null;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptMybatisService.findMapper(eruptModel.getClazz()).insert(object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptMybatisService.findMapper(eruptModel.getClazz()).updateById(object);
    }

    @Override
    @SneakyThrows
    public void deleteData(EruptModel eruptModel, Object object) {
        Field pk = object.getClass().getDeclaredField(eruptModel.getErupt().primaryKeyCol());
        pk.setAccessible(true);
        EruptMybatisService.findMapper(eruptModel.getClazz()).deleteById((Serializable) pk.get(object));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DataProcessorManager.register(EruptMybatisConst.MYBATIS_PROCESS, this.getClass());
    }
}
