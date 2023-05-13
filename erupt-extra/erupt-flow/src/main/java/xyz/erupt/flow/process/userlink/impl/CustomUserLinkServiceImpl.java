package xyz.erupt.flow.process.userlink.impl;

import org.springframework.stereotype.Service;
import xyz.erupt.flow.bean.vo.OrgTreeVo;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserLinkServiceImpl extends DefaultUserLinkServiceImpl {

    /**
     * 自定义的用户体系service要重写优先级，并且返回值要大于0
     * @return
     */
    @Override
    public int priority() {
        return 1;
    }


    /**
     * 针对某些方法进行改写
     * 返回指定部门的主管
     * @return
     */
    private List<OrgTreeVo> getLeadersByDeptId(Long deptId) {
        //我直接返回空集，没有部门主管
        return new ArrayList<>(0);
    }
}
