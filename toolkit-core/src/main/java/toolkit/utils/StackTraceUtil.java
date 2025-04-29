package toolkit.utils;

import cn.hutool.core.util.StrUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StackTraceUtil {
    public static String getStackTrace(Exception e) {
        StringWriter out = new StringWriter();
        PrintWriter s = new PrintWriter(out);
        e.printStackTrace(s);
        s.close();
        return out.toString();
    }

    public static String getStackTracesString(){
        return stackTraceToString(Thread.currentThread().getStackTrace());
    }

    public static String stackTraceToString(StackTraceElement[] stackTraces){
        return StrUtil.sub(Arrays.stream(stackTraces).map(StackTraceElement::toString).collect(Collectors.joining(",")), 0, 1000);
    }
}
