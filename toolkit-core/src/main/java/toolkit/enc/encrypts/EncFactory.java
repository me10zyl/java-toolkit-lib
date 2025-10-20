package toolkit.enc.encrypts;


import toolkit.enc.dto.EncEnum;

public class EncFactory {


    public static EncryptAlogritm getEncryptAlogritm(EncEnum algorithm) {
        if (algorithm.equals(EncEnum.SM4_ECB)) {
            return new SM4ECBEncryptAlogritm();
        } else if (algorithm.equals(EncEnum.SM2)) {
            return new SM2EncryptAlogritm();
        } else if(algorithm.equals(EncEnum.AES)){
            return new AESEncryptAlogritm();
        }else if(algorithm.equals(EncEnum.SM4_CBC)){
            return new SM4CBCEncryptAlogritm();
        }else if(algorithm.equals(EncEnum.MD5)){
            return new MD5HashAlogritm();
        }else if(algorithm.equals(EncEnum.SM3)){
            return new SM3HashAlogritm();
        }else if(algorithm.equals(EncEnum.RSA)){
            return new RSAEncryptAlogritm();
        }
        throw new IllegalArgumentException("不支持的加密算法: " + algorithm);
    }
}
