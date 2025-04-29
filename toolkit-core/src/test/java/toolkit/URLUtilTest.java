package toolkit;


import org.junit.jupiter.api.Test;
import toolkit.utils.URLUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class URLUtilTest {

    @Test
    public void testGetPath_BaseUrlNull() {
        String result = URLUtil.getPath(null, "test");
        assertEquals("test", result);
    }

    @Test
    public void testGetPath_PathWithoutSlash() {
        String result = URLUtil.getPath("http://example.com", "test");
        assertEquals("http://example.com/test", result);
    }

    @Test
    public void testGetPath_PathWithSlash() {
        String result = URLUtil.getPath("http://example.com", "/test");
        assertEquals("http://example.com/test", result);
    }

    @Test
    public void testGetPath_BaseUrlEndsWithSlash() {
        String result = URLUtil.getPath("http://example.com/", "test");
        assertEquals("http://example.com/test", result);
    }
    @Test
    public void testGetPath_BaseUrlEndsWithSlash2() {
        String result = URLUtil.getPath("http://example.com/", "/test");
        assertEquals("http://example.com/test", result);
    }

    @Test
    public void testAddQueryParam_UrlNull() {
        String result = URLUtil.addQueryParam(null, "key", "value");
        assertNull(result);
    }

    @Test
    public void testAddQueryParam_KeyOrValueNull() {
        String url = "http://example.com";
        assertEquals(url, URLUtil.addQueryParam(url, null, "value"));
        assertEquals(url, URLUtil.addQueryParam(url, "key", null));
        assertEquals(url, URLUtil.addQueryParam(url, null, null));
    }

    @Test
    public void testAddQueryParam_FirstParam() {
        String result = URLUtil.addQueryParam("http://example.com", "name", "张三");
        assertEquals("http://example.com?name=%E5%BC%A0%E4%B8%89", result);
    }

    @Test
    public void testAddQueryParam_AdditionalParam() {
        String result = URLUtil.addQueryParam("http://example.com?q=test", "page", "1");
        assertEquals("http://example.com?q=test&page=1", result);
    }
}