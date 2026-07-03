package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * Static callout rendered inside the form for user-defined descriptive content.
 * The owning field is display-only and should be marked @Transient — its value
 * is never collected by the form nor persisted to the database.
 *
 * @author YuePeng
 * date 2026/7/3
 */
public @interface CalloutType {

    @Comment("Callout content, supports HTML")
    String value() default "";

    @Comment("CARD renders a bordered panel; the others render an alert banner")
    Style style() default Style.CARD;

    enum Style {
        CARD, INFO, SUCCESS, WARNING, ERROR
    }

}
