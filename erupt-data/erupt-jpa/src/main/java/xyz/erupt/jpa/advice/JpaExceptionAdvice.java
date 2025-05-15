package xyz.erupt.jpa.advice;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.R;

/**
 * @author YuePeng
 * date 2025/5/13 20:44
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 2)
@ControllerAdvice
public class JpaExceptionAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleHibernateConstraint(ConstraintViolationException ex) {
        log.error("JDBC ConstraintViolationException: ", ex);
        String sqlState = ex.getSQLState();
        int errorCode = ex.getErrorCode();
        ResponseEntity.BodyBuilder rb = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        // MySQL
        if (1451 == errorCode) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.delete_fail_may_be_associated_data")));
        } else if (1062 == errorCode) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.data_duplication")));
        } else if (1265 == errorCode) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.limit_length")));
        }
        // SQL Server
        if (547 == errorCode) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.delete_fail_may_be_associated_data")));
        } else if (2627 == errorCode || 2601 == errorCode) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.data_duplication")));
        } else if (8152 == errorCode) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.limit_length")));
        }
        // PostgreSQL
        if ("23503".equals(sqlState)) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.delete_fail_may_be_associated_data")));
        } else if ("23505".equals(sqlState)) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.data_duplication")));
        } else if ("22003".equals(sqlState)) {
            return rb.body(R.error("Number out of range"));
        } else if ("23000".equals(sqlState)) {
            return rb.body(R.error(ex.getMessage()));
        }
        // Oracle
        if ("2291".equals(sqlState)) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.delete_fail_may_be_associated_data")));
        } else if ("1".equals(sqlState)) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.data_duplication")));
        } else if ("12899".equals(sqlState)) {
            return rb.body(R.error(I18nTranslate.$translate("erupt.data.limit_length")));
        }
        throw ex;
    }

}
