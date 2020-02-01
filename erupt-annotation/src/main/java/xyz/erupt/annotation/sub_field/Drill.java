package xyz.erupt.annotation.sub_field;

public @interface Drill {

    Class<?> eruptClass();

    String id() default "id";

    String joinColumn();
}
