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
import toolkit.enc.encrypts.EncFactory;
import toolkit.enc.properties.EncProperties;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping("/api/test")
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
//        bytes = Base64.getDecoder().decode("Yw2dvWWLmJNqt19zxHdvVQ==");
        new SecureRandom().nextBytes(bytes);
        EncryptAlogritm sm2 = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.RSA);
        EncryptAlogritm sm4 = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.SM4_ECB);
        String publicKeyHex = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100a17b5c10f57e4815c2577a4ac9417c770c462d9466d109b4a20f85ddd814c359990d1b869e2c392ee9e9ad434af627aedc5eb1dd48d98ecc97c4df1c826119e0d0a6701ce2763b1a6977cd344f6edb261c28912aaf5def5ddcfd1c0fb775c38880e6aa7d28913b47463bbb02f1982aeca7ccc7e959f0e1a6d7e2adf518440ee5bd6c5b56360d842858b70775690afb3380e751513a03f06620bd8862d3c93ec5d7266755822edd51e89c7326596b15c3841bac927013825d4d4826f93719feadf26507f2c7e2ea97b13b5acc4e0d7f7f059a2dc5a585b83c3714540a37858164c45625151681bbaabf6f9c281b83d995028bfdc1ccda2c08c92d156f6cd7cf5b0203010001";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        jsonObject.put("nonce", RandomUtil.randomString(32));
        HttpEncBody httpEncBody = genEncBody(sm2, publicKeyHex, bytes, sm4, jsonObject);

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("sm4KeyRanBytesBase64", Base64.getEncoder().encodeToString(bytes));
        jsonObject1.put("encBody", httpEncBody);
        jsonObject1.put("publicKey" , publicKeyHex);
        jsonObject1.put("originBody" , jsonObject);
        return JSONObject.toJSONString(jsonObject1);
    }

    private HttpEncBody genEncBody(EncryptAlogritm sm2, String publicKeyHex, byte[] bytes, EncryptAlogritm sm4, JSONObject jsonObject) {
        if(true){
            EncryptAlogritm aes = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.AES);
            HttpEncBody httpEncBody = new HttpEncBody();
            String s = aes.encryptToBase64(new String("{}"), encProperties.getAesKey().getBytes(StandardCharsets.UTF_8), null);
            httpEncBody.setEncryptContent(s);
            return httpEncBody;
        }
        HttpEncBody httpEncBody = new HttpEncBody();
        httpEncBody.setEncryptKey(sm2.encryptToBase64(new PublicKey(publicKeyHex),
                bytes));
        httpEncBody.setEncryptContent(sm4.encryptToBase64(jsonObject.toJSONString(), bytes, null));
        httpEncBody.setSignature(genSign(jsonObject, bytes));
        return httpEncBody;
    }

    private String genSign(JSONObject jsonObject, byte[] bytes) {
        EncryptAlogritm md5 = EncFactory.getEncryptAlogritm(EncryptAlogritmEnum.MD5);
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
