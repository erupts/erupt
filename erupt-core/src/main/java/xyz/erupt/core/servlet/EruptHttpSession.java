package xyz.erupt.core.servlet;

public interface EruptHttpSession {

	Object getAttribute(String key);

	void setAttribute(String key, Object value);

	<T> T getSession();
}
