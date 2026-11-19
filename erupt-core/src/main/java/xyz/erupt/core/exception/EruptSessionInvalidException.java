package xyz.erupt.core.exception;

/**
 * The request carries a live token whose session no longer stands for anyone, e.g. the user
 * behind it was deleted or the database was rebuilt while the session store kept its tokens.
 * Answered with 401 so the client signs in again instead of failing on a 500.
 *
 * @author YuePeng
 */
public class EruptSessionInvalidException extends RuntimeException {

    public EruptSessionInvalidException(String message) {
        super(message);
    }

}
