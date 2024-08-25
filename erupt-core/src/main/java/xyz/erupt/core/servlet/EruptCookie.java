package xyz.erupt.core.servlet;

public interface EruptCookie {

	String getName();

	String getValue();

	<T> T getCookie();
}
