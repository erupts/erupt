package xyz.erupt.core.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.AttachmentProxy;

import java.lang.annotation.*;

@Comment("自定义附件上传，需在spring boot入口类中修饰")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptAttachmentUpload {
    Class<? extends AttachmentProxy> value();
}
