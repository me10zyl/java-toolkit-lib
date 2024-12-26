package toolkit.traceid;

import org.slf4j.MDC;

import java.util.UUID;

public class MDCUtil {
    public static void putMDCGlobalTraceId(){
        putMDCGlobalTraceId(UUID.randomUUID().toString().substring(0, 8));
    }

    public static void putMDCGlobalTraceId(String globalTraceId){
        MDC.put(Constants.MDC_GLOBAL_TRACE_ID, globalTraceId);
    }

    public static String getMDCGlobalTraceId(){
        return MDC.get(Constants.MDC_GLOBAL_TRACE_ID);
    }
}
