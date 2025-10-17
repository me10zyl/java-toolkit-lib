package toolkit.enc.encrypts;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    /**
     * 对字符串进行 MD5 散列并返回十六进制字符串
     * @param input 待散列的字符串
     * @return 32位的MD5十六进制字符串，如果出错则返回 null
     */
    public static String getMd5Hex(String input) {
        try {
            // 1. 获取 MD5 算法的 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 2. 更新数据，将字符串转换为字节数组
            md.update(input.getBytes());

            // 3. 生成散列值（16字节）
            byte[] digest = md.digest();

            // 4. 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
