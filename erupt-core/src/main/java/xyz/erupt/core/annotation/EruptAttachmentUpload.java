package xyz.erupt.core.annotation;

import xyz.erupt.annotation.fun.AttachmentProxy;

import java.lang.annotation.*;

/**
 * 自定义附件上传
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptAttachmentUpload {
    Class<? extends AttachmentProxy> value();
}
