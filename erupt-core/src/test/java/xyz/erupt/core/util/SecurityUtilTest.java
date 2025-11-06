package xyz.erupt.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;


public class SecurityUtilTest {

    @Test
    public void testXssInspect() {
        // Script tag
        assert SecurityUtil.xssInspect("123 <script>alert(1)</script>");
        assert SecurityUtil.xssInspect("<script src='evil.js'></script>");
        assert SecurityUtil.xssInspect("<script>document.write('xss')</script>");
        
        // Event handler test
        assert SecurityUtil.xssInspect("<div onclick='alert(1)'></div>");
        assert SecurityUtil.xssInspect("<img onload='alert(1)' src='test.jpg'>");
        assert SecurityUtil.xssInspect("<input onfocus='alert(1)'>");
        assert SecurityUtil.xssInspect("<form onsubmit='alert(1)'>");
        assert SecurityUtil.xssInspect("<body onload='alert(1)'>");
        
        // Fake protocol testing
        assert SecurityUtil.xssInspect("<a href='javascript:alert(1)'>link</a>");
        assert SecurityUtil.xssInspect("<img src='javascript:alert(1)'>");
        assert SecurityUtil.xssInspect("<iframe src='data:text/html,<script>alert(1)</script>'>");
        
        // Dangerous attribute test
        assert SecurityUtil.xssInspect("<img src='javascript:alert(1)'>");
        assert SecurityUtil.xssInspect("<form action='javascript:alert(1)'>");
        
        // Dangerous Function Call Test
        assert SecurityUtil.xssInspect("eval('alert(1)')");
        assert SecurityUtil.xssInspect("setTimeout('alert(1)', 100)");
        assert SecurityUtil.xssInspect("setInterval('alert(1)', 100)");
        assert SecurityUtil.xssInspect("Function('alert(1)')()");
        assert SecurityUtil.xssInspect("expression(alert(1))");
        
        // Dangerous HTML Tags Test
        assert SecurityUtil.xssInspect("<iframe src='evil.html'></iframe>");
        assert SecurityUtil.xssInspect("<object data='evil.swf'></object>");
        assert SecurityUtil.xssInspect("<embed src='evil.swf'>");
        assert SecurityUtil.xssInspect("<applet code='evil.class'>");
        
        // Style expression test
        assert SecurityUtil.xssInspect("div{background:expression(alert(1))}");
        
        // Dangerous DOM Operation Test
        assert SecurityUtil.xssInspect("element.innerHTML='<script>alert(1)</script>'");
        assert SecurityUtil.xssInspect("element.outerHTML='<script>alert(1)</script>'");
        assert SecurityUtil.xssInspect("document.write('<script>alert(1)</script>')");
        assert SecurityUtil.xssInspect("document.writeln('<script>alert(1)</script>')");
    }

    @Test
    public void xssFalsePositive() {
        // Normal content should not be wrongly reported.
        assertFalse(SecurityUtil.xssInspect("This is a normal paragraph with no malicious content."));
        assertFalse(SecurityUtil.xssInspect("User input: Hello World"));
        
        // Normal JavaScript code (not in dangerous contexts)
        assertFalse(SecurityUtil.xssInspect("function normalFunction() { return true; }"));
        assertFalse(SecurityUtil.xssInspect("var x = 5; var y = 10;"));
        assertFalse(SecurityUtil.xssInspect("if (condition) { doSomething(); }"));
        
        // Normal HTML tags (without dangerous attributes)
        assertFalse(SecurityUtil.xssInspect("<div>Normal content</div>"));
        assertFalse(SecurityUtil.xssInspect("<p>This is a paragraph</p>"));
        assertFalse(SecurityUtil.xssInspect("<span>Normal span</span>"));
        assertFalse(SecurityUtil.xssInspect("<h1>Title</h1>"));
        assertFalse(SecurityUtil.xssInspect("<a href='https://example.com'>Normal link</a>"));
        assertFalse(SecurityUtil.xssInspect("<img src='https://example.com/image.jpg' alt='image'>"));
        
        // Standard HTML tags (without dangerous attributes) standard CSS
        assertFalse(SecurityUtil.xssInspect("div { color: red; font-size: 14px; }"));
        assertFalse(SecurityUtil.xssInspect("body { background-color: white; }"));
        
        // Normal URL and path
        assertFalse(SecurityUtil.xssInspect("https://www.example.com"));
        assertFalse(SecurityUtil.xssInspect("/path/to/file.html"));
        assertFalse(SecurityUtil.xssInspect("mailto:user@example.com"));
        
        // Normal form elements
        assertFalse(SecurityUtil.xssInspect("<input type='text' name='username'>"));
        assertFalse(SecurityUtil.xssInspect("<button type='submit'>Submit</button>"));
        assertFalse(SecurityUtil.xssInspect("<select><option>Option 1</option></select>"));
        
        // Normal text content
        assertFalse(SecurityUtil.xssInspect("Email: user@example.com"));
        assertFalse(SecurityUtil.xssInspect("Phone: +1-234-567-8900"));
        assertFalse(SecurityUtil.xssInspect("Address: 123 Main St, City, State"));
    }

}
