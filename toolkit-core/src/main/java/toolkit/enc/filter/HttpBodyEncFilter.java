package toolkit.enc.filter;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import toolkit.dto.Constants;
import toolkit.enc.dto.EncryptAlogritmEnum;
import toolkit.enc.dto.HttpEncBody;
import toolkit.enc.dto.PrivateKey;
import toolkit.enc.dto.PublicKey;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncryptFactory;
import toolkit.enc.encrypts.MD5Util;
import toolkit.enc.properties.EncProperties;
import toolkit.enc.wrapper.EncryptResponseWrapper;
import toolkit.enc.wrapper.RepeatableReadRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 全局请求体解密过滤器
 */
@Order(1) // 确保在其他可能读取请求体的组件之前执行
@Slf4j
public class HttpBodyEncFilter implements Filter {

    private final EncProperties encProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String[] excludePatterns;
    private final ObjectMapper objectMapper;
    private final Supplier<Boolean> isTestEnv;

    public HttpBodyEncFilter(EncProperties encProperties, String[] excludePatterns, Supplier<Boolean> isTestEnv, ObjectMapper objectMapper) {
        this.encProperties = encProperties;
        this.excludePatterns = excludePatterns;
        this.isTestEnv = isTestEnv;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 1. 检查并转换请求对象
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (request.getContentType() != null && request.getContentType().contains(
                "multipart/form-data")) {
            chain.doFilter(request, response);
            return;
        }
        // 使用 AntPathMatcher 进行模式匹配
        if (excludePatterns != null) {
            boolean matched = Arrays.stream(excludePatterns).anyMatch(pattern -> {
                return pathMatcher.match(pattern, ((HttpServletRequest) request).getRequestURI());
            });
            if (matched) {
                chain.doFilter(httpServletRequest, response);
                return;
            }
        }

        // 2. 检查是否需要加密 (例如：只处理 POST/PUT 请求，并检查特定的 Header)
        if (!isDecryptionRequired(httpServletRequest)) {
            chain.doFilter(httpServletRequest, response);
            return;
        }

        // 3. 读取原始请求体 (只能读一次)
        byte[] encryptedBody = StreamUtils.copyToByteArray(httpServletRequest.getInputStream());
        String encryptedText = new String(encryptedBody, StandardCharsets.UTF_8);

        // 4. 执行解密逻辑 (此处应集成您的 SM4 解密方法)
        String decryptedText = null;
        try {
            decryptedText = performDecryption(encryptedText);
        } catch (Exception e) {
            // 记录错误或返回错误响应
            throw new RuntimeException("Decrypt request body failed", e);
        }
        if (encProperties.isLogDecrypt()) {
            log.info("Decrypted request body: {}", decryptedText);
        }



        // 5. 创建包含解密后数据的 Request Wrapper
        byte[] decryptedBodyBytes = decryptedText.getBytes(StandardCharsets.UTF_8);
        RepeatableReadRequestWrapper wrappedRequest =
                new RepeatableReadRequestWrapper(httpServletRequest, decryptedBodyBytes);

        // 6. 将包装后的请求对象传递给后续的过滤器链和 DispatcherServlet
        chain.doFilter(wrappedRequest, new EncryptResponseWrapper((HttpServletResponse) response, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return doEncrypt(s);
            }
        }));
    }

    private void checkSign(String decryptedText, String sign, String key) {
        try {
            JsonNode jsonNode = objectMapper.readTree(decryptedText);
            //排序
            String timestamp = jsonNode.get("timestamp").asText();
            if(StrUtil.isBlank(timestamp)){
                throw new RuntimeException("timestamp为空");
            }
            String nonce = jsonNode.get("nonce").asText();
            if(StrUtil.isBlank(timestamp)){
                throw new RuntimeException("nonce为空");
            }
            SimpleDateFormat yyyyMMssHHmmss = new SimpleDateFormat("yyyyMMssHHmmss");
            Date parse = yyyyMMssHHmmss.parse(timestamp);
            if((parse.getTime() - System.currentTimeMillis()) > 5 * 60 * 10000){
                throw new RuntimeException("timestamp错误");
            }
            if(sign == null){
                throw new RuntimeException("sign为空");
            }

            EncryptAlogritm md5 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.MD5);
            if(sign.equals(md5.hash((timestamp + nonce + key).getBytes()))){
                throw new RuntimeException("sign错误");
            }
//            JSONObject jsonObject = JSONObject.parseObject(decryptedText);
//            jsonObject.entrySet().stream()
//                    .filter(e->e.getValue() != null)
//                    .sorted((a,b )->{
//                return a.getKey().compareTo(b.getKey());
//            }).map(e->e.getKey() + "=" + e.getValue());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String doEncrypt(String originStr) {
        byte[] sm4Key = new byte[16];
        new SecureRandom().nextBytes(sm4Key);
        EncryptAlogritm sm4 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM4_ECB);
        String encryptedText = sm4.encryptToBase64(originStr, sm4Key, null);
        EncryptAlogritm sm2 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM2);
        HttpEncBody httpEncBody = new HttpEncBody();
        httpEncBody.setEncryptKey(sm2.encryptToBase64(new PublicKey(encProperties.getSm2PublicKeyHex2()), sm4Key));
        httpEncBody.setEncryptContent(encryptedText);
        try {
            return objectMapper.writeValueAsString(httpEncBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] readAllBytes(InputStream inputStream) {
        // 读取所有字节
        try {
            return StreamUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Read request body failed", e);
        }

    }

    // --- 辅助方法 ---

    private boolean isDecryptionRequired(HttpServletRequest request) {
        if (!encProperties.isEnabled()) {
            return false;
        }
        // 仅拦截 POST 和 PUT 请求
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            return false;
        }


        if (isTestEnv != null && isTestEnv.get() && request.getHeader(Constants.DISABLE_ENC_HEADER) != null) {
            log.info("Disable encrypt for test request");
            return false;
        }

        return true; // 默认对所有 POST/PUT 请求进行处理
    }

    private String performDecryption(String encryptedText) throws Exception {
        HttpEncBody httpEncBody = JSONObject.parseObject(encryptedText, HttpEncBody.class);
        EncryptAlogritm sm2 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM2);
        EncryptAlogritm sm4 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM4_ECB);
        byte[] sm4Key = sm2.decryptFromBase64(new PrivateKey(encProperties.getSm2PrivateKeyHex()), httpEncBody.getEncryptKey());
        String s = sm4.decryptFromBase64(httpEncBody.getEncryptContent(), sm4Key, null);
        checkSign(s, httpEncBody.getSignature(), MD5.create().digestHex(sm4Key));
        return s;
    }
}