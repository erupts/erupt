package xyz.erupt.http;

import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the two kinds of placeholder allowed in {@code @EruptHttp} strings, on every
 * request so the values can change between calls:
 * <ul>
 *   <li>{@code ${key}} — Spring property placeholders, for secrets kept in configuration</li>
 *   <li>{@code #{expr}} — SpEL with {@link Root} as root object, so {@code #{user.tenantId}}
 *       reads the current session and {@code #{@bean.method()}} calls any Spring bean</li>
 * </ul>
 * Strings without a placeholder pass through untouched, including when no Spring
 * context is running.
 *
 * @author YuePeng
 */
public final class HttpTemplate {

    private static final String PROPERTY_PREFIX = "${";

    private static final String EXPR_PREFIX = "#{";

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private static final TemplateParserContext TEMPLATE = new TemplateParserContext(EXPR_PREFIX, "}");

    private static final Map<String, Expression> EXPRESSIONS = new ConcurrentHashMap<>();

    private HttpTemplate() {
    }

    /**
     * What a {@code #{...}} expression can see: {@code user} is the current
     * {@link MetaUser}, or {@code null} outside a request.
     */
    public record Root(MetaUser user) {
    }

    public static String resolve(String template) {
        String value = template;
        ApplicationContext context = EruptSpringUtil.getApplicationContext();
        if (value.contains(PROPERTY_PREFIX) && null != context) {
            value = context.getEnvironment().resolvePlaceholders(value);
        }
        if (value.contains(EXPR_PREFIX)) {
            StandardEvaluationContext evaluation = new StandardEvaluationContext(new Root(currentUser()));
            if (null != context) evaluation.setBeanResolver(new BeanFactoryResolver(context));
            Expression expression = EXPRESSIONS.computeIfAbsent(value, v -> PARSER.parseExpression(v, TEMPLATE));
            value = String.valueOf(expression.getValue(evaluation, String.class));
        }
        return value;
    }

    private static MetaUser currentUser() {
        try {
            return MetaContext.getUser();
        } catch (RuntimeException outsideRequest) {
            return null;
        }
    }

}
