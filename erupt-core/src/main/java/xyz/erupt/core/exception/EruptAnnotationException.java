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
            throw ExceptionAnsi.styleEruptException(eruptModel,
                    "Primary key not found. Please verify if the primary key column name is "
                            + eruptModel.getErupt().primaryKeyCol()
                            + ". If you don't want to define the primary key name as '" + eruptModel.getErupt().primaryKeyCol()
                            + "', you can modify the @erupt->primaryKeyCol value to resolve this exception");
        }
    }

}
