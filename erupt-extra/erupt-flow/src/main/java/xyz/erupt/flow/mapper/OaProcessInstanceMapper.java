package xyz.erupt.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.erupt.flow.bean.entity.OaProcessInstance;

@Mapper
public interface OaProcessInstanceMapper extends BaseMapper<OaProcessInstance> {
}
