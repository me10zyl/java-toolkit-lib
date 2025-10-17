package toolkit.enc.encrypts;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Security;

/**
 * SM3 哈希算法工具类
 * <p>
 * 基于 Bouncy Castle 实现。
 */
public class SM3Util {

    static {
        // 为了确保算法可用，静态注册 Bouncy Castle 提供者
        // 如果环境中已经注册，此操作无副作用
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 计算字符串的SM3哈希值
     *
     * @param inputBytes 待计算的字节数组
     * @return 64位的十六进制哈希值
     */
    public static String hash(byte[] inputBytes) {
        if (inputBytes == null) {
            return null;
        }

        SM3Digest digest = new SM3Digest();
        digest.update(inputBytes, 0, inputBytes.length);

        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);

        return Hex.toHexString(hash);
    }

    /**
     * 计算文件的SM3哈希值
     *
     * @param filePath 文件路径
     * @return 64位的十六进制哈希值, 如果文件读取失败则返回null
     */
    public static String hashFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            SM3Digest digest = new SM3Digest();
            byte[] buffer = new byte[4096]; // 使用稍大的缓冲区以提高效率
            int length;
            while ((length = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }

            byte[] hash = new byte[digest.getDigestSize()];
            digest.doFinal(hash, 0);

            return Hex.toHexString(hash);
        } catch (IOException e) {
            // 在实际应用中，建议使用日志框架记录异常
            e.printStackTrace();
            return null;
        }
    }
}
