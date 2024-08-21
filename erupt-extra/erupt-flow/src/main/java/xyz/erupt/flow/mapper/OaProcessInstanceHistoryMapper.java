package xyz.erupt.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.erupt.flow.bean.entity.OaProcessInstanceHistory;

@Mapper
public interface OaProcessInstanceHistoryMapper extends BaseMapper<OaProcessInstanceHistory> {
}
