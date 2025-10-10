package xyz.erupt.core.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SecurityUtilTest {

    @Test
    public void testXssInspect() {
        // Script 标签测试
        assert SecurityUtil.xssInspect("123 <script>alert(1)</script>");
        assert SecurityUtil.xssInspect("<script src='evil.js'></script>");
        assert SecurityUtil.xssInspect("<script>document.write('xss')</script>");
        
        // 事件处理器测试
        assert SecurityUtil.xssInspect("<div onclick='alert(1)'></div>");
        assert SecurityUtil.xssInspect("<img onload='alert(1)' src='test.jpg'>");
        assert SecurityUtil.xssInspect("<input onfocus='alert(1)'>");
        assert SecurityUtil.xssInspect("<form onsubmit='alert(1)'>");
        assert SecurityUtil.xssInspect("<body onload='alert(1)'>");
        
        // 伪协议测试
        assert SecurityUtil.xssInspect("<a href='javascript:alert(1)'>link</a>");
        assert SecurityUtil.xssInspect("<img src='javascript:alert(1)'>");
        assert SecurityUtil.xssInspect("<iframe src='data:text/html,<script>alert(1)</script>'>");
        
        // 危险属性测试
        assert SecurityUtil.xssInspect("<img src='javascript:alert(1)'>");
        assert SecurityUtil.xssInspect("<form action='javascript:alert(1)'>");
        
        // 危险函数调用测试
        assert SecurityUtil.xssInspect("eval('alert(1)')");
        assert SecurityUtil.xssInspect("setTimeout('alert(1)', 100)");
        assert SecurityUtil.xssInspect("setInterval('alert(1)', 100)");
        assert SecurityUtil.xssInspect("Function('alert(1)')()");
        assert SecurityUtil.xssInspect("expression(alert(1))");
        
        // 危险HTML标签测试
        assert SecurityUtil.xssInspect("<iframe src='evil.html'></iframe>");
        assert SecurityUtil.xssInspect("<object data='evil.swf'></object>");
        assert SecurityUtil.xssInspect("<embed src='evil.swf'>");
        assert SecurityUtil.xssInspect("<applet code='evil.class'>");
        
        // 样式表达式测试
        assert SecurityUtil.xssInspect("div{background:expression(alert(1))}");
        
        // 危险DOM操作测试
        assert SecurityUtil.xssInspect("element.innerHTML='<script>alert(1)</script>'");
        assert SecurityUtil.xssInspect("element.outerHTML='<script>alert(1)</script>'");
        assert SecurityUtil.xssInspect("document.write('<script>alert(1)</script>')");
        assert SecurityUtil.xssInspect("document.writeln('<script>alert(1)</script>')");
    }

    @Test
    public void xssFalsePositive() {
        // 正常内容不应该被误报
        assertFalse(SecurityUtil.xssInspect("This is a normal paragraph with no malicious content."));
        assertFalse(SecurityUtil.xssInspect("User input: Hello World"));
        
        // 正常的JavaScript代码（不在危险上下文中）
        assertFalse(SecurityUtil.xssInspect("function normalFunction() { return true; }"));
        assertFalse(SecurityUtil.xssInspect("var x = 5; var y = 10;"));
        assertFalse(SecurityUtil.xssInspect("if (condition) { doSomething(); }"));
        
        // 正常的HTML标签（不包含危险属性）
        assertFalse(SecurityUtil.xssInspect("<div>Normal content</div>"));
        assertFalse(SecurityUtil.xssInspect("<p>This is a paragraph</p>"));
        assertFalse(SecurityUtil.xssInspect("<span>Normal span</span>"));
        assertFalse(SecurityUtil.xssInspect("<h1>Title</h1>"));
        assertFalse(SecurityUtil.xssInspect("<a href='https://example.com'>Normal link</a>"));
        assertFalse(SecurityUtil.xssInspect("<img src='https://example.com/image.jpg' alt='image'>"));
        
        // 正常的CSS
        assertFalse(SecurityUtil.xssInspect("div { color: red; font-size: 14px; }"));
        assertFalse(SecurityUtil.xssInspect("body { background-color: white; }"));
        
        // 正常的URL和路径
        assertFalse(SecurityUtil.xssInspect("https://www.example.com"));
        assertFalse(SecurityUtil.xssInspect("/path/to/file.html"));
        assertFalse(SecurityUtil.xssInspect("mailto:user@example.com"));
        
        // 正常的表单元素
        assertFalse(SecurityUtil.xssInspect("<input type='text' name='username'>"));
        assertFalse(SecurityUtil.xssInspect("<button type='submit'>Submit</button>"));
        assertFalse(SecurityUtil.xssInspect("<select><option>Option 1</option></select>"));
        
        // 正常的文本内容
        assertFalse(SecurityUtil.xssInspect("Email: user@example.com"));
        assertFalse(SecurityUtil.xssInspect("Phone: +1-234-567-8900"));
        assertFalse(SecurityUtil.xssInspect("Address: 123 Main St, City, State"));
    }

}
