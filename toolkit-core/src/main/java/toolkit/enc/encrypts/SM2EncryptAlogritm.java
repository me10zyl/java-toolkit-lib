package toolkit.enc.encrypts;



import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import toolkit.enc.dto.KeyPair;
import toolkit.enc.dto.PrivateKey;
import toolkit.enc.dto.PublicKey;

import java.math.BigInteger;

public class SM2EncryptAlogritm implements EncryptAlogritm {

    @Override
    public byte[] decryptFromBase64(PrivateKey privateKey, String encryptedDataBase64) {
        return SM2Util.decrypt(SM2Util.stringToPrivateKey(privateKey.getPrivateKeyHex()), encryptedDataBase64);
    }

    @Override
    public String encryptToBase64(PrivateKey privateKey, byte[] data) {
        return SM2Util.encrypt(SM2Util.stringToPrivateKey(privateKey.getPrivateKeyHex()), data);
    }

    @Override
    public String encryptToBase64(PublicKey publicKey, byte[] data) {
        return SM2Util.encrypt(SM2Util.stringToPublicKey(publicKey.getPublicKeyHex()), data);
    }

    @Override
    public KeyPair generateKeyPair() {
        AsymmetricCipherKeyPair keyPair = SM2Util.generateKeyPair();
        ECPrivateKeyParameters privateKey1 = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters publicKey1 = (ECPublicKeyParameters) keyPair.getPublic();
        PrivateKey privateKey = new PrivateKey(SM2Util.privateKeyToString(privateKey1));
        PublicKey publicKey = new PublicKey(SM2Util.publicKeyToString(publicKey1));
        return new KeyPair(privateKey, publicKey);
    }

    @Override
    public PublicKey getPublicKey(PrivateKey privateKey) {
        ECPoint Q = SM2Util.getPublicKeyFromPrivateKey(new BigInteger(privateKey.getPrivateKeyHex(), 16));
        byte[] publicKeyBytes = Q.getEncoded(false);
        return new PublicKey(Hex.toHexString(publicKeyBytes));
    }

    public static void main(String[] args) {
        SM2EncryptAlogritm sm2 = new SM2EncryptAlogritm();
        KeyPair keyPair = sm2.generateKeyPair();
        //private:00b945d7ae08c0f50382df712c09849edf3dd46a2cf17973e4ab105a3de8e1db8a
        //public:04a5111979c8af40389cb6df3358bc0669f2e7b9241fbf291d1419983ff4133c9159fae6242414e7d2775b3af3e6d9f05dce0481601e8c4fcbb6846ea217f3c074
        System.out.println(keyPair);
        System.out.println(sm2.getPublicKey(keyPair.getPrivateKey()));
    }

    //后端私钥：009bee2085e9ecbdc43b83982f55e0b2a80644f9f143afde542394c3a5653ad886
    //后端公钥: 049bd1dd0b056fea9deb618719e298482c1322dae87bea89def099ef122b8817f41b1f64f50b4501d3f97e3caa66bfd4d190b13f2f6716816fd8491a6f5f238ee6

}



