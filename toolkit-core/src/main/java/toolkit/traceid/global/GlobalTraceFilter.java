package toolkit.traceid.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import toolkit.traceid.Constants;
import toolkit.traceid.MDCUtil;
import toolkit.traceid.properties.GlobalTraceProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalTraceFilter extends OncePerRequestFilter {

    private Boolean hasDubbo = null;
    private final GlobalTraceProperties globalTraceProperties;
    public GlobalTraceFilter(GlobalTraceProperties globalTraceProperties) {
        this.globalTraceProperties = globalTraceProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            long startTime = System.currentTimeMillis();
            String globalTraceId = (String) request.getAttribute(Constants.REQ_GLOBAL_TRACE_ID);
            if (globalTraceId == null) {
                globalTraceId = MDCUtil.getMDCGlobalTraceId();
            }
            try {
                if(hasDubbo == null) {
                    Class.forName("org.apache.dubbo.rpc.RpcContext");
                    String dubboTraceId = RpcContext.getServerAttachment().getAttachment(Constants.DUBBO_CONTEXT_TRACE_ID);
                    if (dubboTraceId != null) {
                        globalTraceId = dubboTraceId;
                    }
                    hasDubbo = true;
                }
            }catch (ClassNotFoundException e){
                hasDubbo = false;
            }
            if (globalTraceId == null) {
                globalTraceId = UUID.randomUUID().toString().substring(0, 8);
                MDCUtil.putMDCGlobalTraceId(globalTraceId);
            }
            request.setAttribute(Constants.REQ_GLOBAL_TRACE_ID, globalTraceId);
            response.setHeader(Constants.X_REQUEST_ID, globalTraceId);
            if(hasDubbo){
                RpcContext.getServerAttachment().setAttachment(Constants.DUBBO_CONTEXT_TRACE_ID, globalTraceId);
            }
            String clientIp = getClientIp(request);
            MDC.put("clientIp", clientIp);
            if(globalTraceProperties.isEnableLog()) {
                log.info("Request started - Method: {}, URI: {}, QueryString: {}",
                        request.getMethod(), request.getRequestURI(), request.getQueryString());
            }
            filterChain.doFilter(request, response);
            // 8. 记录请求完成日志
            if(globalTraceProperties.isEnableLog()) {
                long duration = System.currentTimeMillis() - startTime;
                log.info("Request completed - Status: {}, Duration: {}ms", response.getStatus(), duration);
            }
        } finally {
            MDCUtil.removeMDCGlobalTraceId();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
