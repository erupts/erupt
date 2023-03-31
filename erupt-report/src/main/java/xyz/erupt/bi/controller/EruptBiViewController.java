//package xyz.erupt.bi.controller;
//
//import org.springframework.web.bind.annotation.*;
//import xyz.erupt.bi.constant.BiConst;
//import xyz.erupt.bi.model.BiView;
//import xyz.erupt.bi.view.BiViewVo;
//import xyz.erupt.jpa.dao.EruptDao;
//
//import javax.annotation.Resource;
//import javax.transaction.Transactional;
//import java.util.List;
//
///**
// * @author YuePeng
// * date 2022/7/19 00:33
// */
//@RestController
//@RequestMapping(BiConst.BI_VIEW)
//public class EruptBiViewController {
//
//    @Resource
//    private EruptDao eruptDao;
//
//    //列表
//    @GetMapping("/list")
//    public List<BiViewVo> list() {
//        return eruptDao.queryEntityList(BiViewVo.class);
//    }
//
//    //预览
//    @GetMapping("/preview/{id}")
//    public void preview(@PathVariable Long id) {
//
//    }
//
//    //详情
//    @GetMapping("/detail/{id}")
//    public BiViewVo detail(@PathVariable Long id) {
//        return eruptDao.getEntityManager().find(BiViewVo.class, id);
//    }
//
//    //创建
//    @PostMapping("/create")
//    public void create(String name) {
//
//    }
//
//    //更新
//    @PostMapping("/update/{id}")
//    public void update(@PathVariable Long id, String config) {
//
//    }
//
//    //重命名
//    @Transactional
//    @GetMapping("rename/{id}")
//    public void rename(@PathVariable Long id, String name) {
//        BiView biView = eruptDao.getEntityManager().find(BiView.class, id);
//        biView.setName(name);
//        eruptDao.mergeAndFlush(biView);
//    }
//
//    //发布
//    @Transactional
//    @GetMapping("/publish/{id}")
//    public void publish(@PathVariable Long id) {
//        BiView biView = eruptDao.getEntityManager().find(BiView.class, id);
//        biView.setPublish(true);
//        eruptDao.mergeAndFlush(biView);
//    }
//
//    //取消发布
//    @Transactional
//    @GetMapping("/cancel-publish/{id}")
//    public void cancelPublish(@PathVariable Long id) {
//        BiView biView = eruptDao.getEntityManager().find(BiView.class, id);
//        biView.setPublish(false);
//        eruptDao.mergeAndFlush(biView);
//    }
//
//    //克隆
//    @GetMapping("/clone/{id}")
//    public void cloneView(@PathVariable Long id) {
//
//    }
//
//    //删除
//    @GetMapping("/delete/{id}")
//    @Transactional
//    public void delete(@PathVariable Long id) {
//        eruptDao.getEntityManager().remove(new BiViewVo() {{
//            this.setId(id);
//        }});
//    }
//
//
//}
