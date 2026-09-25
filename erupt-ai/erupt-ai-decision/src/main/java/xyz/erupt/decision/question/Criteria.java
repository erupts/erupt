package xyz.erupt.decision.question;

/**
 * Implemented by an enum used as {@link Choice} options, to describe what each constant means.
 * The model reads the rubric, not the constant name, so a described enum decides far better.
 *
 * @author YuePeng
 */
public interface Criteria {

    String criteria();

}
