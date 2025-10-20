package toolkit.enc.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.StreamUtils;
import toolkit.enc.dto.EncryptAlogritmEnum;
import toolkit.enc.dto.HttpEncBody;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncFactory;
import toolkit.enc.properties.EncProperties;
import toolkit.enc.util.CommonUtil;
import toolkit.enc.wrapper.RepeatableReadRequestWrapper;
import toolkit.enc.exception.EncException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        this.commonUtil = new CommonUtil(encProperties, environment, excludePatterns, testEnvProfiles);
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

        // 2. 检查是否需要加密 (例如：只处理 POST/PUT 请求，并检查特定的 Header)
        if (!commonUtil.isDecryptionRequired(httpServletRequest, null)) {
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
            throw new EncException(StrUtil.format("Decrypt request body failed（encryptedText:{}）", encryptedText), e);
        }
        if (encProperties.isLogDecrypt()) {
            log.info("Decrypted request body: {}", decryptedText);
        }


        // 5. 创建包含解密后数据的 Request Wrapper
        byte[] decryptedBodyBytes = decryptedText.getBytes(StandardCharsets.UTF_8);
        RepeatableReadRequestWrapper wrappedRequest =
                new RepeatableReadRequestWrapper(httpServletRequest, decryptedBodyBytes);

        // 6. 将包装后的请求对象传递给后续的过滤器链和 DispatcherServlet
        chain.doFilter(wrappedRequest, response);
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
            SimpleDateFormat yyyyMMssHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
            Date parse = yyyyMMssHHmmss.parse(timestamp.asText());
            if ((parse.getTime() - System.currentTimeMillis()) > 5 * 60 * 10000) {
                throw new EncException("timestamp错误");
            }
            if (sign == null) {
                throw new EncException("sign为空");
            }

            EncryptAlogritm md5 = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.MD5);
            String s = timestamp.asText() + nonce.asText() + md5.hash(key);
            log.info("originStr:" + s);
            if (!sign.equals(md5.hash(s.getBytes()))) {
                throw new EncException("sign错误");
            }
//            JSONObject jsonObject = JSONObject.parseObject(decryptedText);
//            jsonObject.entrySet().stream()
//                    .filter(e->e.getValue() != null)
//                    .sorted((a,b )->{
//                return a.getKey().compareTo(b.getKey());
//            }).map(e->e.getKey() + "=" + e.getValue());
        } catch (JsonProcessingException e) {
            throw new EncException(e);
        } catch (ParseException e) {
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

    private String performDecryption(String encryptedText) throws Exception {
        HttpEncBody httpEncBody = JSONObject.parseObject(encryptedText, HttpEncBody.class);
//        EncryptAlogritm rsa = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.RSA);
//        EncryptAlogritm sm4 = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM4_ECB);
//        byte[] sm4Key = rsa.decryptFromBase64(new PrivateKey(encProperties.getRsaPrivateKeyHex()), httpEncBody.getEncryptKey());
        EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.AES);
//        String sm4Key = encProperties.getSm4KeyHex();
        String s = aes.decryptFromBase64(httpEncBody.getEncryptContent(), encProperties.getAesKey().getBytes(StandardCharsets.UTF_8), null );
                //;sm4.decryptFromBase64(httpEncBody.getEncryptContent(),Hex.decode(sm4Key), null);
//        checkSign(s, httpEncBody.getSignature(), sm4Key.get);
        return s;
    }


}