package toolkit.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @since 2024/6/26 上午10:19
 **/
@Slf4j
public class IpUtil {

    public static String getContextIpAddr(){
        return getClientIpAddress(HttpContextUtil.getHttpServletRequest());
    }

    public static String getLocalIP() {
        List<String> allLocalIPs = getAllLocalIPs();
        if (allLocalIPs.isEmpty()) {
            return null;
        }
        return allLocalIPs.get(0);
    }

    /**
     * 获取所有非回环（Non-Loopback）的本地 IP 地址列表。
     *
     * @return 包含所有有效本地 IP 地址的列表。
     */
    public static List<String> getAllLocalIPs() {
        List<String> ipList = new ArrayList<>();
        try {
            // 遍历所有的网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                // 排除虚拟接口、未启用的接口和回环接口
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }

                // 遍历该网络接口下的所有IP地址
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // 排除回环地址和链路本地地址（169.254.x.x）
                    if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        String ip = addr.getHostAddress();

                        // 仅添加 IPv4 地址（如果需要，可以添加 IPv6 的过滤逻辑）
                        if (ip.contains(".")) {
                            ipList.add(ip);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
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
