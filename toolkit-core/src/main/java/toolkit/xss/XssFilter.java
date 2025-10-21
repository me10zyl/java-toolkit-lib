package toolkit.xss;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * 对前段请求的参数进行xss过滤 注意当使用@RequestBody类型json参数时未过滤，如果有html的参数手动调用JsoupUtil.clean(）过滤
 * 原因  JsoupUtil.clean(） 过滤后会导致json 格式错误，如果转义前段是直接文字输出 显示有误
 */

public class XssFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request), response);
    }


}
