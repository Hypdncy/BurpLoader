package com.hypdncy.BurpLoader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class KeyFilter {
    private static byte[] encryption_key = "burpr0x!".getBytes();

    public KeyFilter() {
    }

    private static byte[] decrypt(byte[] data) {
        try {
            SecretKeySpec spec = new SecretKeySpec(encryption_key, "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(2, spec);
            return cipher.doFinal(data);
        } catch (Exception var3) {
            var3.printStackTrace();
            throw new RuntimeException(var3);
        }
    }

    public static void test(Object[] obj) {
        byte[] data = (byte[]) obj[0];
        byte[] decode = Base64.getDecoder().decode(data);
        byte[] decrypt = decrypt(decode);
        String str = new String(decrypt);
        String[] strs = str.split("\u0000");
        obj[0] = Arrays.copyOf(strs, strs.length - 2);
    }
}
