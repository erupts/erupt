package xyz.erupt.core.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.AttachmentProxy;

import java.lang.annotation.*;

@Comment("Custom attachment upload, must be applied on the Spring Boot entry class")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptAttachmentUpload {
    Class<? extends AttachmentProxy> value();
}
