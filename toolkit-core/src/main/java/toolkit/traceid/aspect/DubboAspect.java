package toolkit.traceid.aspect;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.stereotype.Component;
import toolkit.traceid.Constants;
import toolkit.traceid.PrefixMatchJoinPoint;
import toolkit.traceid.SerializeExclude;


import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@Aspect
@Slf4j
public class DubboAspect extends AbstractPointcutAdvisor {


    @Setter
    private SerializeExclude[] serializeExcludes = new SerializeExclude[]{
    };
    private final String aopPrefix;
    private final Class<? extends RuntimeException> serviceExceptionClass;

    public DubboAspect(String aopPrefix,  Class<? extends RuntimeException> serviceExceptionClass) {
        this.aopPrefix = aopPrefix;
        this.serviceExceptionClass = serviceExceptionClass;
    }

    public Object around(MethodInvocation point) throws Throwable {
        StringBuilder sb = new StringBuilder();
        StringBuilder argsBuilder = new StringBuilder();
        Object[] args = point.getArguments();
        for (int i = 0; i < args.length; i++) {
            argsBuilder.append(JSONObject.toJSONString(args[i]));
            if (i != args.length - 1) {
                argsBuilder.append(",");
            }
        }
        sb.append("发起DUBBO请求：")
                .append(point.getMethod().getDeclaringClass().getSimpleName())
                .append("#")
                .append(point.getMethod().getName())
                .append("(")
                .append(argsBuilder)
                .append(")");
        Object proceed = null;
        Exception exp = null;
        int errType = 0;
        long start = System.currentTimeMillis();
        String consumerTag = System.getProperty("dubbo.consumer.tag");
        try {
            if(StrUtil.isNotBlank(consumerTag)) {
                RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, consumerTag);
                sb.append("[TAG:" + consumerTag + "]");
            }
            RpcContext.getServerAttachment().setAttachment(Constants.DUBBO_CONTEXT_TRACE_ID, MDC.get(Constants.MDC_GLOBAL_TRACE_ID));
            proceed = point.proceed();
        } catch (RpcException e) {
            exp = e;
            errType = 0;
            RuntimeException runtimeException = serviceExceptionClass.getConstructor(String.class).newInstance("上游请求不通:上游RPC出错");
            throw runtimeException;
        }catch (Exception e){
            exp = e;
            if(e.getClass().equals(RuntimeException.class)) {
                errType = 1;
                String message = "上游返错:" + e.getMessage();
                throw serviceExceptionClass.getConstructor(String.class).newInstance(message);
            }else{
                errType = 2;
                log.error("上游异常：", e);
                throw e;
            }
        } finally {
            if (exp != null) {
                if (errType == 0) {
                    sb.append(" RPC异常：" + exp.toString() + ":" + exp.getMessage());
                } else if(errType == 1){
                    sb.append(" 反错：" + exp.toString() + ":" + exp.getMessage());
                } else{
                    sb.append(" 上游异常：" + exp.toString() + ":" + exp.getMessage());
                }
            } else {
                SimplePropertyPreFilter filter = new SimplePropertyPreFilter(){
                    @Override
                    public boolean apply(JSONSerializer serializer, Object source, String name) {
                        boolean apply = super.apply(serializer, source, name);
                        if(apply){
                            return Arrays.stream(serializeExcludes).noneMatch(e->{
                                return e.getClazz().isInstance(source) && Arrays.stream(e.getPropertyNames()).collect(Collectors.toList()).contains(name);
                            });
                        }
                        return apply;
                    }
                };
                sb.append(" 响应：" + JSONObject.toJSONString(proceed, filter));
            }
            sb.append(" 花费:")
                    .append(System.currentTimeMillis() - start)
                    .append("ms");
            log.info(sb.toString());
        }
        return proceed;
    }

    @Override
    public Pointcut getPointcut() {
        return new PrefixMatchJoinPoint(aopPrefix);
    }

    @Override
    public Advice getAdvice() {
        return new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                return around(methodInvocation);
            }
        };
    }
}
