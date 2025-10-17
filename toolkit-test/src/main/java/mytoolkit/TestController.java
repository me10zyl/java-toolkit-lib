package mytoolkit;


import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import toolkit.enc.dto.EncryptAlogritmEnum;
import toolkit.enc.dto.HttpEncBody;
import toolkit.enc.dto.PublicKey;
import toolkit.enc.encrypts.EncryptAlogritm;
import toolkit.enc.encrypts.EncryptFactory;
import toolkit.enc.properties.EncProperties;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private EncProperties encProperties;

    @GetMapping("/gen")
    public String gen() {
        return genBody();
    }

    @RequestMapping("/echo")
    public String echo(HttpServletRequest request, @RequestBody(required = false) String body) {
        //打印所有参数，headers，等等
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> result = new HashMap<>();
        result.put("url", request.getRequestURL());
        result.put("method", request.getMethod());
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        result.put("headers", headers);
        result.put("parameters", parameterMap);
        result.put("body", body);
        return JSONObject.toJSONString(result);

    }

    private String genBody() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        EncryptAlogritm sm2 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM2);
        EncryptAlogritm sm4 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM4_ECB);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        jsonObject.put("nonce", RandomUtil.randomString(32));
        HttpEncBody httpEncBody = new HttpEncBody();
        httpEncBody.setEncryptKey(sm2.encryptToBase64(new PublicKey("0498e149fcee64727322b2dc273e57577c1794afe4be40b90d055936840113381593563f01189d55b3616745924d63f05ee3010cba1354149b386589024150bb20"),
                bytes));
        httpEncBody.setEncryptContent(sm4.encryptToBase64(jsonObject.toJSONString(), bytes, null));
        httpEncBody.setSignature(genSign(jsonObject, bytes));
        String jsonString = JSONObject.toJSONString(httpEncBody);
        System.out.println(jsonString);
        return jsonString;
    }

    private String genSign(JSONObject jsonObject, byte[] bytes) {
        EncryptAlogritm md5 = EncryptFactory.getEncryptAlogritm(EncryptAlogritmEnum.MD5);
        String timestamp = jsonObject.getString("timestamp");
        String nonce = jsonObject.getString("nonce");
        String hash = md5.hash(bytes);
        System.out.println(timestamp);
        System.out.println(nonce);
        System.out.println(hash );
        String s = timestamp + nonce + hash;
        System.out.println(s);
        return md5.hash(s.getBytes());
    }
}
