package toolkit.enc.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.StreamUtils;
import toolkit.enc.dto.*;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncFactory;
import toolkit.enc.properties.EncProperties;
import toolkit.enc.util.CommonUtil;
import toolkit.enc.wrapper.RepeatableReadRequestWrapper;
import toolkit.enc.exception.EncException;
import toolkit.utils.StackTraceUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * 全局请求体解密过滤器
 */
@Order(1) // 确保在其他可能读取请求体的组件之前执行
@Slf4j
public class HttpBodyEncFilter implements Filter {

    private final EncProperties encProperties;
    private final ObjectMapper objectMapper;
    private final CommonUtil commonUtil;

    public HttpBodyEncFilter(EncProperties encProperties, ObjectMapper objectMapper, Environment environment, String[] testEnvProfiles, String[] excludePatterns) {
        this.encProperties = encProperties;
        this.objectMapper = objectMapper;
        this.commonUtil = new CommonUtil(encProperties, environment, combineSwagger(excludePatterns, environment), testEnvProfiles);
    }

    private String[] combineSwagger(String[] excludePatterns, Environment environment) {
        if (!encProperties.isExcludeSwagger()) {
            return excludePatterns;
        }
        String contextPath = environment.getProperty("server.servlet.context-path");
        //排除swagger路径
        if (contextPath == null) {
            contextPath = "";
        }
        String[] swaggerPaths = {
                contextPath + "/swagger-resources",
                contextPath + "/v2/api-docs/**",
                contextPath + "/doc.html",
                contextPath + "/api/swagger-resources",
                contextPath + "/api/v2/api-docs/**",
                contextPath + "/api/doc.html"
        };
        String[] combined = new String[excludePatterns.length + swaggerPaths.length];
        System.arraycopy(excludePatterns, 0, combined, 0, excludePatterns.length);
        System.arraycopy(swaggerPaths, 0, combined, excludePatterns.length, swaggerPaths.length);
        return combined;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {

            // 1. 检查并转换请求对象
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if (request.getContentType() != null && request.getContentType().contains(
                    "multipart/form-data")) {
                chain.doFilter(request, response);
                return;
            }

            boolean matchedExclude = commonUtil.excludePatternsMatched(httpServletRequest, null);
            // 2. 检查是否需要加密 (例如：只处理 POST/PUT 请求，并检查特定的 Header)
            if (!commonUtil.isDecryptionRequired(httpServletRequest, null)) {
                if (!matchedExclude && httpServletRequest.getMethod().equals("GET")
                && !commonUtil.hasDisableHeader(httpServletRequest, null) && !commonUtil.disableProperties()) {
                    httpServletRequest.setAttribute(Constants.ATTR_NAME, true);
                }
                chain.doFilter(httpServletRequest, response);
                return;
            }


            // 3. 读取原始请求体 (只能读一次)
            byte[] encryptedBody = StreamUtils.copyToByteArray(httpServletRequest.getInputStream());
            String encryptedText = new String(encryptedBody, StandardCharsets.UTF_8);

            // 4. 执行解密逻辑 (此处应集成您的 SM4 解密方法)
            String decryptedText = null;
            HttpEncBody httpEncBody = null;
            boolean isUrlEncoded = "application/x-www-form-urlencoded".equals(request.getContentType());
            try {
                if (isUrlEncoded) {
                    httpEncBody = new HttpEncBody();
                    HttpEncBody finalHttpEncBody = httpEncBody;
                    Arrays.stream(encryptedText.split("&")).forEach(item -> {
                        String[] keyValue = item.split("=");
                        if (keyValue.length == 2) {
                            if ("encryptContent".equals(keyValue[0])) {
                                finalHttpEncBody.setEncryptContent(decodeUrl(keyValue[1]));
                            } else if ("encryptKey".equals(keyValue[0])) {
                                finalHttpEncBody.setEncryptKey(decodeUrl(keyValue[1]));
                            } else if ("signature".equals(keyValue[0])) {
                                finalHttpEncBody.setSignature(decodeUrl(keyValue[1]));
                            }
                        }
                    });
                } else {
                    httpEncBody = JSONObject.parseObject(encryptedText, HttpEncBody.class);
                }
            } catch (Exception e) {

            }
            if (matchedExclude && (httpEncBody == null || httpEncBody.getEncryptContent() == null)) {
                log.info("matchedExclude and no EncContent, not decrypt {}", httpServletRequest.getRequestURI());
                chain.doFilter(new RepeatableReadRequestWrapper(httpServletRequest, encryptedBody, false), response);
                return;
            }
            try {
                decryptedText = performDecryption(httpEncBody);
            } catch (Exception e) {
                // 记录错误或返回错误响应
                throw new EncException(StrUtil.format("Decrypt request body failed（encryptedText:{}）", encryptedText), e);
            }
            if (encProperties.isLogDecrypt()) {
                log.info("Decrypted request body: {}", decryptedText);
            }
            if (encProperties.isCheckSign()) {
                checkSign(decryptedText, httpEncBody.getSignature(), encProperties.getAesKey().getBytes(StandardCharsets.UTF_8));
            }

            // 5. 创建包含解密后数据的 Request Wrapper
            byte[] decryptedBodyBytes = decryptedText == null ? new byte[0] : decryptedText.getBytes(StandardCharsets.UTF_8);
            RepeatableReadRequestWrapper wrappedRequest =
                    new RepeatableReadRequestWrapper(httpServletRequest, decryptedBodyBytes, isUrlEncoded);

            // 6. 将包装后的请求对象传递给后续的过滤器链和 DispatcherServlet
            wrappedRequest.setAttribute(Constants.ATTR_NAME, true);
            chain.doFilter(wrappedRequest, response);
        } catch (EncException e) {
            log.error("加密异常: {}", e.getMessage(), e);
            handleException((HttpServletResponse) response, e);
        }
    }

    private String decodeUrl(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new EncException(e);
        }
    }

    private void handleException(HttpServletResponse response, EncException e) {
        if (commonUtil.isIsTestEnv()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(e instanceof EncException ? "ENC_ERROR" : "FILTER_ERROR");
            errorResponse.setMessage(e.getMessage());
            errorResponse.setStackTrace(StackTraceUtil.getStackTrace(e));
            errorResponse.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).replace(" ", "T"));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            try {
                String json = objectMapper.writeValueAsString(errorResponse);

                response.getWriter().write(json);
                response.getWriter().flush();
            } catch (Exception ex) {
                log.error("write errorResponse failed: {}", ex.getMessage(), ex);
            }
        } else {
            throw e;
        }

    }

    @Data
    static class ErrorResponse {
        private String code;
        private String message;
        private String timestamp;
        private String stackTrace;
    }

    private void checkSign(String decryptedText, String sign, byte[] key) {
        try {
            JsonNode jsonNode = objectMapper.readTree(decryptedText);
            //排序
            JsonNode timestamp = jsonNode.get("timestamp");
            if (timestamp == null || StrUtil.isBlank(timestamp.asText())) {
                throw new EncException("timestamp为空");
            }
            JsonNode nonce = jsonNode.get("nonce");
            if (nonce == null || StrUtil.isBlank(nonce.asText())) {
                throw new EncException("nonce为空");
            }
            if (sign == null) {
                throw new EncException("sign为空");
            }
            Date parse = new SimpleDateFormat("yyyyMMddHHmmss").parse(timestamp.asText());
            if ((parse.getTime() - System.currentTimeMillis()) > 5 * 60 * 10000) {
                throw new EncException("timestamp有偏差");
            }

            EncryptAlogritm md5 = EncFactory.getEncryptAlogritm(EncEnum.MD5);
            String s = timestamp.asText() + nonce.asText() + md5.hash(key);
            log.info("originStr:" + s);
            if (!sign.equals(md5.hash(s.getBytes()))) {
                throw new EncException("sign错误");
            }
//
        } catch (JsonProcessingException | ParseException e) {
            throw new EncException(e);
        }
    }


    private byte[] readAllBytes(InputStream inputStream) {
        // 读取所有字节
        try {
            return StreamUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            throw new EncException("Read request body failed", e);
        }

    }

    private String performDecryption(HttpEncBody httpEncBody) throws Exception {
        if (httpEncBody == null || httpEncBody.getEncryptContent() == null) {
            return null;
        }
        String decryptText = null;
        if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.AES)) {
            EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncEnum.AES);
            decryptText = aes.decryptFromBase64(httpEncBody.getEncryptContent(), encProperties.getAesKey().getBytes(StandardCharsets.UTF_8), null);
        } else if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.RSA_AES)) {
            EncryptAlogritm rsa = EncFactory.getEncryptAlogritm(EncEnum.RSA);
            EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncEnum.AES);
            byte[] key = rsa.decryptFromBase64(new PrivateKey(base64ToHex(encProperties.getRsaPrivateKeyBase64())), httpEncBody.getEncryptContent());
            decryptText = aes.decryptFromBase64(httpEncBody.getEncryptContent(), key, null);
        } else if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.SM4)) {
            EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncEnum.AES);
            decryptText = aes.decryptFromBase64(httpEncBody.getEncryptContent(), encProperties.getSm4Key().getBytes(StandardCharsets.UTF_8), null);
        } else if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.SM2_SM4)) {
            EncryptAlogritm sm2 = EncFactory.getEncryptAlogritm(EncEnum.SM2);
            EncryptAlogritm sm4 = EncFactory.getEncryptAlogritm(EncEnum.SM4_ECB);
            byte[] key = sm2.decryptFromBase64(new PrivateKey(encProperties.getSm2PrivateKeyHex()), httpEncBody.getEncryptContent());
            decryptText = sm4.decryptFromBase64(httpEncBody.getEncryptContent(), key, null);
        }
        return decryptText;
    }

    private String base64ToHex(String rsaPrivateKeyBase64) {
        return Hex.toHexString(Base64.getDecoder().decode(rsaPrivateKeyBase64));
    }


}