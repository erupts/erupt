package xyz.erupt.flow.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.erupt.flow.bean.entity.OaTask;

import java.util.List;

@Mapper
public interface OaTaskMapper extends MPJBaseMapper<OaTask> {

    List<OaTask> listMyTasks(QueryWrapper<OaTask> queryWrapper);
}
