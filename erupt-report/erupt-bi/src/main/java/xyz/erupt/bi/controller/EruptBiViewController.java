package xyz.erupt.bi.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.view.BiViewVo;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/7/19 00:33
 */
@RestController
@RequestMapping(BiConst.BI_VIEW)
public class EruptBiViewController {

    @Resource
    private EruptDao eruptDao;

    //列表
    @RequestMapping("/list")
    public List<BiViewVo> list() {
        return eruptDao.queryEntityList(BiViewVo.class);
    }

    //预览
    @RequestMapping("/preview/{code}")
    public void preview(@PathVariable String code) {

    }

    //详情
    @RequestMapping("/detail/{id}")
    public BiViewVo detail(@PathVariable Long id) {
        return eruptDao.getEntityManager().find(BiViewVo.class, id);
    }

    //创建
    @RequestMapping("/create")
    public void create(String name) {

    }

    //更新
    @RequestMapping("/update/{id}")
    public void update(@PathVariable Long id, String config) {

    }

    //重命名
    @RequestMapping("rename/{id}")
    public void rename(@PathVariable Long id, String name) {

    }

    //发布
    @RequestMapping("/publish/{id}")
    public void publish(@PathVariable Long id) {

    }

    //取消发布
    @RequestMapping("/cancel-publish/{id}")
    public void cancelPublish(@PathVariable Long id) {

    }

    //克隆
    @RequestMapping("/clone/{id}")
    public void cloneView(@PathVariable Long id) {

    }

    //删除
    @RequestMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        eruptDao.getEntityManager().remove(new BiViewVo() {{
            this.setId(id);
        }});
    }


}
