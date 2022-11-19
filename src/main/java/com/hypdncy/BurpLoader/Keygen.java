package com.hypdncy.BurpLoader;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

/* loaded from: BurpLoaderKeygen.jar:com/h3110w0r1d/burploaderkeygencn/Keygen.class */
public class Keygen {
    private static final byte[] encryption_key = "burpr0x!".getBytes();

    private static byte[] encrypt(byte[] arrayOfByte) {
        try {
            SecretKeySpec localSecretKeySpec = new SecretKeySpec(encryption_key, "DES");
            Cipher localCipher = Cipher.getInstance("DES");
            localCipher.init(1, localSecretKeySpec);
            return localCipher.doFinal(arrayOfByte);
        } catch (Exception var4) {
            var4.printStackTrace();
            throw new RuntimeException(var4);
        }
    }

    private static byte[] decrypt(byte[] arrayOfByte) {
        try {
            SecretKeySpec localSecretKeySpec = new SecretKeySpec(encryption_key, "DES");
            Cipher localCipher = Cipher.getInstance("DES");
            localCipher.init(2, localSecretKeySpec);
            return localCipher.doFinal(arrayOfByte);
        } catch (Exception var3) {
            var3.printStackTrace();
            throw new RuntimeException(var3);
        }
    }


    private String getRandomString() {
        StringBuilder str = new StringBuilder();
        Random rnd = new Random();
        while (str.length() < 32) {
            int index = (int) (rnd.nextFloat() * "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".length());
            str.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".charAt(index));
        }
        return str.toString();
    }

    private static ArrayList<String> getParamsList(String data) {
        byte[] rawBytes = decrypt(Base64.getDecoder().decode(data));
        ArrayList<String> ar = new ArrayList<>();
        int from = 0;
        for (int i = 0; i < rawBytes.length; i++) {
            if (rawBytes[i] == 0) {
                ar.add(new String(rawBytes, from, i - from));
                from = i + 1;
            }
        }
        ar.add(new String(rawBytes, from, rawBytes.length - from));
        return ar;
    }

    private String prepareArray(ArrayList<String> list) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            for (int i = 0; i < list.size() - 1; i++) {
                byteArray.write(list.get(i).getBytes());
                byteArray.write(0);
            }
            byteArray.write(list.get(list.size() - 1).getBytes());
            return new String(Base64.getEncoder().encode(encrypt(byteArray.toByteArray())));
        } catch (Exception var3) {
            var3.printStackTrace();
            throw new RuntimeException(var3);
        }
    }

    private static ArrayList<String> decodeActivationRequest(String activationRequest) {
        try {
            ArrayList<String> ar = getParamsList(activationRequest);
            if (ar.size() != 5) {
                System.out.print("Activation Request Decoded to wrong size! The following was Decoded: \n");
                System.out.print(ar);
                return null;
            }
            return ar;
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }


    public String generateLicense(String licenseName) {

        LocalDate localDate = LocalDate.of(2099,12,30);
        Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        long unixTimestamp = instant.toEpochMilli();

        ArrayList<String> licenseArray = new ArrayList<>();
        licenseArray.add(getRandomString());
        licenseArray.add("license");
        licenseArray.add(licenseName);
        licenseArray.add(String.valueOf(unixTimestamp));
        licenseArray.add("1");
        licenseArray.add("full");
        licenseArray.add("I don't care SHA256withRSA");
        licenseArray.add("I don't care SHA1withRSA");
        return prepareArray(licenseArray);
    }

    public String generateActivation(String activationRequest) {

        ArrayList<String> request = decodeActivationRequest(activationRequest);
        if (request == null) {
            return "Error decoding activation request :-(";
        }
        ArrayList<String> responseArray = new ArrayList<>();
        responseArray.add("0.4315672535134567");
        responseArray.add(request.get(0));
        responseArray.add("activation");
        responseArray.add(request.get(1));
        responseArray.add("True");
        responseArray.add("");
        responseArray.add(request.get(2));
        responseArray.add(request.get(3));
        responseArray.add("I don't care SHA256withRSA");
        responseArray.add("I don't care SHA1withRSA");
        return prepareArray(responseArray);
    }
}