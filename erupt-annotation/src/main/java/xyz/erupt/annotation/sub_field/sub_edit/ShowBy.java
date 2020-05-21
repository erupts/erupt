package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2020-05-21
 */
public @interface ShowBy {

    String dependField();

    /**
     * 使用eval解析的前端表达式
     * 变量：fieldValue 表示field前端值
     */
    String expr();

}
