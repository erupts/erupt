package xyz.erupt.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.erupt.flow.bean.entity.OaUsers;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author : willian fu
 * @date : 2022/6/27
 */
@Mapper
public interface OaUsersMapper extends BaseMapper<OaUsers> {

    @Select("SELECT ou.user_id id, ou.user_name `name`, 'user' AS 'type', ou.avatar " +
            "FROM oa_user_departments oud LEFT JOIN oa_users ou ON ou.user_id = oud.user_id " +
            "WHERE oud.dept_id = #{deptId}")
    List<OrgTreeVo> selectUsersByDept(@Param("deptId") Integer deptId);

    @Select("SELECT ou.user_id id, ou.user_name `name`, 'user' AS 'type', ou.avatar " +
            "FROM oa_users ou WHERE  pingyin LIKE '%${py}%' OR py LIKE '%${py}%'")
    List<OrgTreeVo> selectUsersByPy(@Param("py") String py);
}
