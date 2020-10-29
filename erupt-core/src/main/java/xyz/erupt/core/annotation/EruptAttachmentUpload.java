package xyz.erupt.core.annotation;

import xyz.erupt.annotation.fun.AttachmentProxy;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptAttachmentUpload {
    Class<? extends AttachmentProxy> value();
}
