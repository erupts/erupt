package xyz.erupt.core.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public interface EruptHttpServletResponse {

	public void setHeader(String name, String value);

	public void addHeader(String name, String value);

	public void sendRedirect(String location) throws IOException;

	public void addCookie(EruptCookie cookie);

	public void setContentType(String contentType);

	public void setCharacterEncoding(String characterEncoding);

	public OutputStream getOutputStream() throws IOException;

	public Collection<String> getHeaderNames();

	public <T> T getResponse();
}
