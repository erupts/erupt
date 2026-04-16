package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface OperationHandler<@Comment("row data type") Row, @Comment("form input object type") EruptForm> {

    /**
     * @param data      row data
     * @param eruptForm form input data
     * @param param     annotation callback parameters
     * @return JS expression to be executed by the frontend after the event is triggered successfully
     */
    @Comment("Button event trigger class")
    @Comment("Return value: JS expression to be executed by the frontend after the event is triggered successfully; return null if not needed")
    String exec(List<Row> data, EruptForm eruptForm, String[] param);

    @Comment("Initialize the values of the erupt form")
    default EruptForm eruptFormValue(List<Row> data, EruptForm eruptForm, String[] param) {
        return eruptForm;
    }

}
