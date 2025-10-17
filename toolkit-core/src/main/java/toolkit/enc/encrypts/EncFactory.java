package toolkit.enc.encrypts;


import toolkit.enc.dto.EncryptAlogritmEnum;

public class EncFactory {


    public static EncryptAlogritm getEncryptAlogritm(EncryptAlogritmEnum algorithm) {
        if (algorithm.equals(EncryptAlogritmEnum.SM4_ECB)) {
            return new SM4ECBEncryptAlogritm();
        } else if (algorithm.equals(EncryptAlogritmEnum.SM2)) {
            return new SM2EncryptAlogritm();
        } else if(algorithm.equals(EncryptAlogritmEnum.AES)){
            return new AESEncryptAlogritm();
        }else if(algorithm.equals(EncryptAlogritmEnum.SM4_CBC)){
            return new SM4CBCEncryptAlogritm();
        }else if(algorithm.equals(EncryptAlogritmEnum.MD5)){
            return new MD5HashAlogritm();
        }else if(algorithm.equals(EncryptAlogritmEnum.SM3)){
            return new SM3HashAlogritm();
        }else if(algorithm.equals(EncryptAlogritmEnum.RSA)){
            return new RSAEncryptAlogritm();
        }
        throw new IllegalArgumentException("不支持的加密算法: " + algorithm);
    }
}
