package toolkit.traceid.aspect;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import lombok.Setter;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import toolkit.traceid.Constants;
import toolkit.traceid.PrefixMatchJoinPoint;
import toolkit.traceid.SerializeExclude;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * http请求切面 - 记录日志
 *
 * @author zyl
 * @date 2022/05/31
 */

public class HttpRequestAspect extends AbstractPointcutAdvisor {

    public static final Logger logger = LoggerFactory.getLogger(HttpRequestAspect.class);

    private final String aopPrefix;

    public HttpRequestAspect(String aopPrefix) {
        Assert.notNull(aopPrefix, "aopPrefix cannot be null");
        this.aopPrefix = aopPrefix;
    }

    @Setter
    private String[] excludeUrls = new String[]{};
    @Setter
    private  SerializeExclude[] httpSerializeExcludes = new SerializeExclude[]{
    };


    public Object around(MethodInvocation invocation) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String globalTraceId = (String) request.getAttribute(Constants.REQ_GLOBAL_TRACE_ID);
        Object[] args = invocation.getArguments();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            try {
                sb.append(JSONObject.toJSONString(args[i])).append("，");
            }catch (Exception e){
                sb.append(args[i]).append("，");
            }
        }
        if(sb.length() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        String url = request.getRequestURL().toString();
        if(Arrays.asList(excludeUrls).contains(request.getRequestURI())){
            return invocation.proceed();
        }
        Object proceed = null;
        Throwable exception = null;
        logger.info("==========请求开始" + url+"==========");
        logger.info("参数：" + sb);
        long start = System.currentTimeMillis();
        try {
            proceed = invocation.proceed();
        }catch (Throwable e){
            exception = e;
        }finally {
            String result = null;
            try {
                SimplePropertyPreFilter filter = new SimplePropertyPreFilter(){
                    @Override
                    public boolean apply(JSONSerializer serializer, Object source, String name) {
                        boolean apply = super.apply(serializer, source, name);
                        if(apply){
                            return Arrays.stream(httpSerializeExcludes).noneMatch(e->{
                                return e.getClazz().isInstance(source) && (e.getPropertyNames() == null || Arrays.stream(e.getPropertyNames()).collect(Collectors.toList()).contains(name));
                            });
                        }
                        return apply;
                    }
                };
                result = JSONObject.toJSONString(proceed, filter);
            }catch (Exception e){
                result = String.valueOf(proceed);
            }
            logger.info("\n----------------请求结束["+globalTraceId+"]"+(exception == null?"成功":"失败")+"----------------\n" +
                    "["+globalTraceId+"]请求URL：" + url + "\n" +
                    "["+globalTraceId+"]请求用户:" + request.getHeader("Authorization") + "\n" +
                    "["+globalTraceId+"]控制器:" + invocation.getThis().getClass().getName() + "#" + invocation.getMethod().getName() + " (" + (System.currentTimeMillis() - start) + "ms)\n" +
                    "["+globalTraceId+"]参数:" + sb.toString() + "\n" +
                    "["+globalTraceId+"]" + (exception == null ? "返回:" + result : "异常:" + exception) + "\n" +
                    "================请求结束["+globalTraceId+"]================");
            if (exception != null) {
                MDC.put("traceParam", sb.toString());
                throw exception;
            }
        }
        return proceed;
    }

    @Override
    public Pointcut getPointcut() {
        return new PrefixMatchJoinPoint(aopPrefix);
    }

    @Override
    public Advice getAdvice() {
        return (MethodInterceptor) this::around;
    }
}
