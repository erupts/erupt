package xyz.erupt.jpa.model;

import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.view.MetaUser;

import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2021/12/20 22:04
 */
public class MetaDataProxy implements DataProxy<BaseModel> {

    @Override
    public void beforeAdd(BaseModel metaModel) {
        if (metaModel instanceof MetaModel) {
            ((MetaModel) metaModel).setCreateTime(LocalDateTime.now());
            ((MetaModel) metaModel).setCreateBy(MetaUser.get().getName());
        } else if (metaModel instanceof MetaModelVo) {
            ((MetaModelVo) metaModel).setCreateTime(LocalDateTime.now());
            ((MetaModelVo) metaModel).setCreateBy(MetaUser.get().getName());
        } else if (metaModel instanceof MetaModelUpdateVo) {
            ((MetaModelUpdateVo) metaModel).setCreateTime(LocalDateTime.now());
            ((MetaModelUpdateVo) metaModel).setCreateBy(MetaUser.get().getName());
        }
    }

    @Override
    public void beforeUpdate(BaseModel metaModel) {
        if (metaModel instanceof MetaModel) {
            ((MetaModel) metaModel).setUpdateTime(LocalDateTime.now());
            ((MetaModel) metaModel).setUpdateBy(MetaUser.get().getName());
        } else if (metaModel instanceof MetaModelVo) {
            ((MetaModelVo) metaModel).setUpdateTime(LocalDateTime.now());
            ((MetaModelVo) metaModel).setUpdateBy(MetaUser.get().getName());
        } else if (metaModel instanceof MetaModelUpdateVo) {
            ((MetaModelUpdateVo) metaModel).setUpdateTime(LocalDateTime.now());
            ((MetaModelUpdateVo) metaModel).setUpdateBy(MetaUser.get().getName());
        }
    }

}
