package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author YuePeng
 * date 2023/5/9 22:31
 */
@Deprecated
public @interface PasswordType {

    //加密次数
    int encryptCount() default 3;


}
