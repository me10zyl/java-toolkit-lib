package toolkit.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 2024/6/26 上午10:19
 **/
@Slf4j
public class IpUtil {

    public static String getContextIpAddr(){
        return getClientIpAddress(HttpContextUtil.getHttpServletRequest());
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 对于多级代理情况，取第一个非 unknown 的 IP
            if (ip != null && ip.length() > 15) {
                if (ip.indexOf(",") > 0) {
                    ip = ip.substring(0, ip.indexOf(","));
                }
            }
            return ip;
        } catch (Exception e) {
            log.error("getClientIpAddress error", e);
        }
        return null;
    }
}
