package xyz.erupt.bi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.bi.constant.BiConst;

/**
 * @author YuePeng
 * date 2022/7/19 00:33
 */
@RestController
@RequestMapping(BiConst.BI_VIEW)
public class EruptBiViewController {

    //列表
    public void list() {

    }

    //预览
    public void preview(String code) {

    }

    //详情
    public void detail(Long id) {

    }

    //创建
    public void create(String name) {

    }

    //更新
    public void update(Long id, String config) {

    }

    //重命名
    public void rename(Long id, String name) {

    }

    //发布
    public void publish(Long id) {

    }

    //取消发布
    public void cancelPublish(Long id) {

    }

    //克隆
    public void cloneView(Long id) {

    }

    //删除
    public void delete(Long id) {

    }


}
