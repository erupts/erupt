package xyz.erupt.core.servlet;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Function;

public interface EruptRequestContextHolder {

	default <R> R convert(Function<ServletRequestAttributes, R> function){
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) requestAttributes);
			return function.apply(servletRequestAttributes);
		}
		return null;
	}

	EruptHttpServletRequest getRequest();

	EruptHttpServletResponse getResponse();
}
