package xyz.erupt.core.servlet;

import org.springframework.http.HttpInputMessage;
import org.springframework.web.multipart.MultipartRequest;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;

public interface EruptHttpServletRequest {

	String getHeader(String name);

	Enumeration<String> getHeaders(String name);

	String getRequestURI();

	String getMethod();

	void setAttribute(String key, Object value);

	String[] getParameterValues(String name);

	Object getAttribute(String name);

	HttpInputMessage getHttpInputMessage();

	String getContentType();

	EruptHttpSession getSession();

	EruptCookie[] getCookies();

	InputStream getInputStream() throws IOException;

	boolean isMultipart();

	String getRemoteAddr();

	MultipartRequest resolveMultipart();

	Principal getUserPrincipal();

	<T> T getRequest();
}


