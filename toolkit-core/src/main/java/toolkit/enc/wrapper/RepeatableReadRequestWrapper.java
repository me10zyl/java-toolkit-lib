package toolkit.enc.wrapper;


import lombok.Getter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 允许重复读取请求体的 Request Wrapper
 */
public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

    @Getter
    private final byte[] body; // 缓存解密后的请求体数据

    private Map<String, String[]> parameter;
    private final boolean isUrlEncoded;//防止post取到queryString上的参数以便攻击

    public RepeatableReadRequestWrapper(HttpServletRequest request, byte[] decryptedBody, boolean isUrlEncoded) {
        super(request);
        this.body = decryptedBody;
        if(isUrlEncoded) {
            this.parameter = parseParameter(body);
        }
        this.isUrlEncoded = isUrlEncoded;
    }

    @Override
    public String getQueryString() {
        return isUrlEncoded ? null : super.getQueryString();
    }

    private Map<String, String[]> parseParameter(byte[] body) {
        if (body == null || body.length == 0) {
            return new HashMap<>();
        }

        // 1. 将字节数组转换为字符串，使用 UTF-8 编码
        // 通常 HTTP 规范推荐使用 UTF-8，但如果请求头指定了其他编码，则应使用该编码。
        String formString = new String(body, StandardCharsets.UTF_8);

        // 用于临时存储解析结果：Key -> List of Values
        Map<String, List<String>> tempParams = new HashMap<>();

        // 2. 按 '&' 拆分键值对
        String[] pairs = formString.split("&");

        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }

            // 3. 按 '=' 拆分键和值
            int idx = pair.indexOf('=');
            String name;
            String value;

            if (idx > 0) {
                // 解析出键名和键值
                name = pair.substring(0, idx);
                value = pair.substring(idx + 1);
            } else {
                // 处理只有键名没有值的情况 (例如: "param1&param2=")
                name = pair;
                value = "";
            }

            // 4. URL 解码（处理 %XX 编码）
            try {
                name = URLDecoder.decode(name, StandardCharsets.UTF_8.name());
                value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                // 理论上 UTF-8 永远支持，此处的异常捕获更多是出于规范要求
                // 实际应用中，可以记录日志或抛出 RuntimeException
                name = name; // 解码失败则使用原始值
                value = value;
            }

            // 5. 将值添加到临时 Map 中
            tempParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        // 6. 转换为 Map<String, String[]> 最终格式
        Map<String, String[]> finalParams = new HashMap<>(tempParams.size());
        for (Map.Entry<String, List<String>> entry : tempParams.entrySet()) {
            List<String> valueList = entry.getValue();
            // 将 List<String> 转换为 String[]
            finalParams.put(entry.getKey(), valueList.toArray(new String[0]));
        }

        return finalParams;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if(!isUrlEncoded){
            return super.getParameterMap();
        }
        return parameter;
    }

    @Override
    public String getParameter(String name) {
        if(!isUrlEncoded){
            return super.getParameter(name);
        }
        String[] values = parameter.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if(!isUrlEncoded){
            return super.getParameterNames();
        }
        return Collections.enumeration(parameter.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if(!isUrlEncoded){
            return super.getParameterValues(name);
        }
        return parameter.get(name);
    }



    /**
     * 重写 getInputStream() 方法，返回包含解密后数据的输入流
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 不支持异步读取
            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    /**
     * 重写 getReader() 方法，返回包含解密后数据的读取器
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }
}