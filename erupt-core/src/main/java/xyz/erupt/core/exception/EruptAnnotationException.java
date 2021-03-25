package xyz.erupt.core.exception;

import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng
 * date 11/1/18.
 */
public class EruptAnnotationException extends RuntimeException {

    public EruptAnnotationException(String message) {
        super(message);
    }

    public static void validateEruptInfo(EruptModel eruptModel) {
        if (null == eruptModel.getEruptFieldMap().get(eruptModel.getErupt().primaryKeyCol())) {
            throw ExceptionAnsi.styleEruptException(eruptModel, "找不到主键,请确认主键列名是否为" + eruptModel.getErupt().primaryKeyCol() +
                    "，如果你不想将主键名定义为'" + eruptModel.getErupt().primaryKeyCol() + "'则可以修改@erupt->primaryKeyCol值解决此异常");
        }
    }

}
