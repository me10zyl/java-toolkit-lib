package toolkit.traceid.aspect;

import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import toolkit.traceid.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class GlobalTraceIdInterceptor implements HandlerInterceptor {
    public GlobalTraceIdInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MDC.put(Constants.MDC_REQUEST_URI, request.getRequestURI());
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String header = request.getHeader(Constants.X_REQUEST_ID);
        if (header != null) {
            traceId = header;
        }

        MDC.put(Constants.MDC_GLOBAL_TRACE_ID, traceId);
        request.setAttribute(Constants.REQ_GLOBAL_TRACE_ID, traceId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Object attribute = request.getAttribute(Constants.REQ_GLOBAL_TRACE_ID);
        if (attribute != null) {
            response.setHeader(Constants.X_REQUEST_ID, attribute.toString());
        }

    }
}
