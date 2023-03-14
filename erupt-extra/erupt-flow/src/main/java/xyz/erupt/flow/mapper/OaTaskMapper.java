package xyz.erupt.flow.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.erupt.flow.bean.entity.OaTask;

@Mapper
public interface OaTaskMapper extends MPJBaseMapper<OaTask> {

//    OaTask getReferenceById(Long taskId);
//
//    void deleteById(Long taskId);
//
//    void saveAndFlush(OaTask build);
//
//    OaTask getStartTaskByInst(Long instId);
//
//    List<OaTask> listMyTasks(String account, String keywords);
//
//    void deleteAllByProcessInstId(Long procInstId);
//
//    void saveAllAndFlush(List<OaTask> oaTasks);
//
//    List<OaTask> findByActivityIdAndActiveOrderByCompleteSortAsc(Long activityId, boolean actived);
}
