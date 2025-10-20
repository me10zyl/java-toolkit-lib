package toolkit.enc.advice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import toolkit.enc.dto.*;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncFactory;
import toolkit.enc.exception.EncException;
import toolkit.enc.properties.EncProperties;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;


@Slf4j
@ControllerAdvice
public class EncryptResponseBodyAdvice implements
        ResponseBodyAdvice<Object> {

    private final EncProperties encProperties;
    private final ObjectMapper objectMapper;

    public EncryptResponseBodyAdvice(EncProperties encProperties, ObjectMapper objectMapper) {
        this.encProperties = encProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }


    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> httpMessageConverter,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {
        // 使用 AntPathMatcher 进行模式匹配
        //情况 文件上传下载，不需要改动，直接返回
        if (body instanceof Resource) {
            return body;
        }
        return encryptResponse(body, serverHttpRequest, serverHttpResponse);
    }


    private Object encryptResponse(Object body, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        boolean decryptionRequired = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getAttribute(Constants.ATTR_NAME) != null;
        if (!decryptionRequired) {
            return body;
        }
        if (encProperties.isLogDecrypt()) {
            try {
                log.info("Decrypted response body: {}", toJson(body));
            } catch (Exception e) {

            }
        }
        boolean isString = false;
        if (body instanceof String) {
            isString = true;
        }
        String jsonString = toJson(body);
        return doEncrypt(jsonString, isString);
    }

    private String toJson(Object body) {
        if (body instanceof String) {
            return (String) body;
        }
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonString;
    }

    private Object doEncrypt(String originStr, boolean isString) {
        if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.AES)) {
            EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncEnum.AES);
            String s = aes.encryptToBase64(originStr, encProperties.getAesKey().getBytes(StandardCharsets.UTF_8), null);
            HttpEncBody encBody = new HttpEncBody();
            encBody.setEncryptContent(s);
            return handleBody(encBody, isString);
        } else if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.SM4)) {
            EncryptAlogritm sm4 = EncFactory.getEncryptAlogritm(EncEnum.SM4_ECB);
            String s = sm4.encryptToBase64(originStr, encProperties.getSm4Key().getBytes(StandardCharsets.UTF_8), null);
            HttpEncBody encBody = new HttpEncBody();
            encBody.setEncryptContent(s);
            return handleBody(encBody, isString);
        } else if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.RSA_AES)) {
            EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncEnum.AES);
            EncryptAlogritm rsa = EncFactory.getEncryptAlogritm(EncEnum.RSA);
            byte[] key = new byte[16];
            new SecureRandom().nextBytes(key);
            String s = aes.encryptToBase64(originStr, key, null);
            HttpEncBody encBody = new HttpEncBody();
            encBody.setEncryptContent(s);
            encBody.setEncryptKey(rsa.encryptToBase64(new PublicKey(base64ToHex(encProperties.getRsaPublicKeyBase64Frontend())), key));
            return handleBody(encBody, isString);
        } else if (encProperties.getEncryptAlgorithm().equals(SupportEncrypt.SM2_SM4)) {
            EncryptAlogritm sm4 = EncFactory.getEncryptAlogritm(EncEnum.SM4_ECB);
            EncryptAlogritm sm2 = EncFactory.getEncryptAlogritm(EncEnum.SM2);
            byte[] key = new byte[16];
            new SecureRandom().nextBytes(key);
            String s = sm4.encryptToBase64(originStr, key, null);
            HttpEncBody encBody = new HttpEncBody();
            encBody.setEncryptContent(s);
            encBody.setEncryptKey(sm2.encryptToBase64(new PublicKey(encProperties.getSm2PublicKeyHexFrontend()), key));
            return handleBody(encBody, isString);
        }else{
            throw new EncException("不支持的加密算法");
        }
    }

    private Object handleBody(HttpEncBody encBody, boolean isString) {
        if (isString) {
            try {
                return objectMapper.writeValueAsString(encBody);
            } catch (JsonProcessingException e) {
                throw new EncException(e);
            }
        }
        return encBody;
    }

    private String base64ToHex(String rsaPrivateKeyBase64) {
        return Hex.toHexString(Base64.getDecoder().decode(rsaPrivateKeyBase64));
    }
}
