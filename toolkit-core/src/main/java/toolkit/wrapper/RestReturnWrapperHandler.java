package toolkit.wrapper;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import toolkit.enc.dto.EncryptAlogritmEnum;
import toolkit.enc.dto.HttpEncBody;
import toolkit.enc.dto.PublicKey;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncryptFactory;
import toolkit.enc.properties.EncProperties;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;


@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class RestReturnWrapperHandler implements
        ResponseBodyAdvice<Object> {

    private final EncProperties encProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();


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
        if (body instanceof HttpEntity) {
            return body;
        } else {
            return encryptResponse(body, serverHttpRequest, serverHttpResponse);
        }
    }

    private Object encryptResponse(Object body, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return doEncrypt(jsonString);
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
}
