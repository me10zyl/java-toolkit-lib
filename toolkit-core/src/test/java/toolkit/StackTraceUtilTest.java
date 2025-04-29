package toolkit;


import org.junit.jupiter.api.Test;
import toolkit.utils.StackTraceUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class StackTraceUtilTest {

    @Test
    public void testGetStackTrace() throws Exception {
        Exception testException = new Exception("Test exception");
        String result = StackTraceUtil.getStackTrace(testException);
        
        // 验证结果包含异常信息
        assertTrue(result.contains("Test exception"));
        assertTrue(result.contains("at toolkit.StackTraceUtilTest.testGetStackTrace"));
        
        // 验证标准输出格式
        StringWriter expectedWriter = new StringWriter();
        testException.printStackTrace(new PrintWriter(expectedWriter));
        assertEquals(expectedWriter.toString(), result);
    }

    @Test
    public void testGetStackTracesString() {
        String result = StackTraceUtil.getStackTracesString();
        
        // 验证结果不为空且长度不超过1000
        assertNotNull(result);
        assertTrue(result.length() <= 1000);
        
        // 验证结果包含当前测试方法
        assertTrue(result.contains("testGetStackTracesString"));
    }

    @Test
    public void testStackTraceToString() {
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        String result = StackTraceUtil.stackTraceToString(traces);
        
        // 验证结果不为空且长度不超过1000
        assertNotNull(result);
        assertTrue(result.length() <= 1000);
        
        // 验证结果包含栈跟踪元素
        assertTrue(result.contains(traces[0].toString()));
    }

    @Test
    public void testStackTraceToString_EmptyArray() {
        StackTraceElement[] emptyTraces = new StackTraceElement[0];
        String result = StackTraceUtil.stackTraceToString(emptyTraces);
        
        // 验证空数组返回空字符串
        assertEquals("", result);
    }

    @Test
    public void testStackTraceToString_LongTrace() {
        // 创建长栈跟踪数组
        StackTraceElement[] longTraces = new StackTraceElement[100];
        Arrays.fill(longTraces, Thread.currentThread().getStackTrace()[0]);
        
        String result = StackTraceUtil.stackTraceToString(longTraces);
        
        // 验证长栈跟踪被截断到1000字符
        assertEquals(1000, result.length());
    }
}