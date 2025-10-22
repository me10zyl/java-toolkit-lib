package toolkit.enc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import toolkit.enc.properties.EncProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;

@Slf4j
public class CommonUtil {
    private final EncProperties encProperties;
    private final Environment environment;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String[] excludePatterns;
    private final String[] testEnvProfiles;


    public CommonUtil(EncProperties encProperties, Environment environment, String[] excludePatterns, String[] testEnvProfiles) {
        this.encProperties = encProperties;
        this.environment = environment;
        this.excludePatterns = excludePatterns;
        this.testEnvProfiles = testEnvProfiles;
    }

    public boolean isDecryptionRequired(HttpServletRequest request, ServletServerHttpRequest request2) {
        if (disableProperties()) return false;
        // 仅拦截 POST 和 PUT 请求
        if (notPost(request, request2)) return false;

        if (hasDisableHeader(request, request2)) return false;

        return true; // 默认对所有 POST/PUT 请求进行处理
    }

    public  boolean notPost(HttpServletRequest request, ServletServerHttpRequest request2) {
        String method = request != null ? request.getMethod() : request2.getMethodValue();

        String requestURI = request != null ? request.getRequestURI() : request2.getServletRequest().getRequestURI();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            log.info("method: {} requestURI: {} not post or put, no encrypt", method, requestURI);
            return true;
        }
        return false;
    }

    public boolean disableProperties() {
        if (!encProperties.isEnabled()) {
            return true;
        }
        return false;
    }

    public boolean hasDisableHeader(HttpServletRequest request, ServletServerHttpRequest request2) {
        boolean isTestEnv = isIsTestEnv();
        boolean header = false;
        if (request == null) {
            header = request2.getHeaders().containsKey(encProperties.getDisableHeader());
        } else {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String next = headerNames.nextElement();
                if (next.equalsIgnoreCase(encProperties.getDisableHeader())) {
                    header = true;
                    break;
                }
            }
        }

        if (isTestEnv && header) {
            log.info("Disable encrypt for test request, not encrypt");
            return true;
        }
        return false;
    }

    public boolean isIsTestEnv() {
        boolean isTestEnv = false;
        if (testEnvProfiles != null && Arrays.stream(testEnvProfiles).anyMatch(profile -> {
            return Arrays.asList(environment.getActiveProfiles()).contains(profile);
        })) {
            isTestEnv = true;
        }
        return isTestEnv;
    }

    public boolean excludePatternsMatched(HttpServletRequest request, ServletServerHttpRequest request2) {
        // 使用 AntPathMatcher 进行模式匹配
        if (excludePatterns != null) {
            boolean matched = Arrays.stream(excludePatterns).anyMatch(pattern -> {
                String requestURI = request != null ? request.getRequestURI() : request2.getServletRequest().getRequestURI();
                return pathMatcher.match(pattern, requestURI);
            });
            if (matched) {
                log.info("excludePattern matched");
                return true;
            }
        }
        return false;
    }
}
