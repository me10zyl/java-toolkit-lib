package toolkit.enc.advice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import toolkit.enc.dto.EncryptAlogritmEnum;
import toolkit.enc.dto.HttpEncBody;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncFactory;
import toolkit.enc.properties.EncProperties;
import toolkit.enc.util.CommonUtil;

import java.nio.charset.StandardCharsets;


@Slf4j
@ControllerAdvice
public class RestReturnWrapperHandler implements
        ResponseBodyAdvice<Object> {

    private final EncProperties encProperties;
    private final ObjectMapper objectMapper;
    private final CommonUtil commonUtil;

    public RestReturnWrapperHandler(EncProperties encProperties, ObjectMapper objectMapper, Environment environment, String[] testEnvProfiles , String[] excludePaths) {
        this.encProperties = encProperties;
        this.objectMapper = objectMapper;
        this.commonUtil = new CommonUtil(encProperties, environment, excludePaths, testEnvProfiles);
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
        boolean decryptionRequired = commonUtil.isDecryptionRequired(null, (ServletServerHttpRequest) serverHttpRequest);
        if(!decryptionRequired){
            return body;
        }
        if(encProperties.isLogDecrypt()){
            try {
                log.info("Decrypted response body: {}", toJson(body));
            } catch (Exception e){

            }
        }
        String jsonString = toJson(body);
        return doEncrypt(jsonString);
    }

    private String toJson(Object body) {
        if(body instanceof String){
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

    private Object doEncrypt(String originStr) {
        EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.AES);
        String s = aes.encryptToBase64(originStr, encProperties.getAesKey().getBytes(StandardCharsets.UTF_8), null);
        HttpEncBody encBody = new HttpEncBody();
        encBody.setEncryptContent(s);
        return toJson(encBody);
//        byte[] sm4Key = new byte[16];
//        new SecureRandom().nextBytes(sm4Key);
//        EncryptAlogritm sm4 = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM4_ECB);
//        String encryptedText = sm4.encryptToBase64(originStr, sm4Key, null);
//        EncryptAlogritm rsa = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.RSA);
//        HttpEncBody httpEncBody = new HttpEncBody();
//        httpEncBody.setEncryptKey(rsa.encryptToBase64(new PublicKey(encProperties.getRsaPublicKeyHex2()), sm4Key));
//        httpEncBody.setEncryptContent(encryptedText);
//        try {
//            return objectMapper.writeValueAsString(httpEncBody);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }
}
