package cn.yuyake.common.utils;

import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESUtils {
    public static String createSecret(long userId, String serverId) {
        // TODO create secret with user id and server id
        return RandomStringUtils.randomAscii(16);
    }

    public static byte[] decode(String secret, byte[] content) {
        try {
            SecretKey key = generateKey(secret);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encode(String secret, byte[] content) {
        try {
            SecretKey key = generateKey(secret);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SecretKey generateKey(String secret) throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(secret.getBytes());
        keygen.init(128, secureRandom);
        SecretKey originalKey = keygen.generateKey();
        byte[] raw = originalKey.getEncoded();
        SecretKey key = new SecretKeySpec(raw, "AES");
        return key;
    }
}
