package xyz.erupt.core.exception;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

/**
 * @author liyuepeng
 * @date 11/1/18.
 */
public class EruptAnnotationException extends RuntimeException {

    public EruptAnnotationException(String message) {
        super(message);
    }


    public static void validateEruptInfo(EruptModel eruptModel) {
        if (null == eruptModel.getEruptFieldMap().get(eruptModel.getErupt().primaryKeyCol())) {
            throw ExceptionUtil.styleEruptException(eruptModel, "找不到主键id,请确认主键列名是否为id，" +
                    "如果你不想将主键名定义为'id'则可以修改@erupt->primaryKeyCol值解决此异常");
        }
        String layoutTree = eruptModel.getErupt().layoutTree();
        if (StringUtils.isNotBlank(layoutTree)) {
            EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(layoutTree);
            if (null == eruptFieldModel) {
                throw ExceptionUtil.styleEruptException(eruptModel, "找不到layoutTree所指定字段");
            }
//            if(!eruptFieldModel.getEruptField().edit().search().value()){
//                throw ExceptionUtil.styleEruptException(eruptModel, "layoutTree所指定的edit类型必须开启搜索项");
//            }
            if (eruptFieldModel.getEruptField().edit().type() != EditType.REFERENCE_TREE) {
                throw ExceptionUtil.styleEruptException(eruptModel, "layoutTree所指定的edit类型必须为" + EditType.REFERENCE_TREE);
            }
        }
    }
}
