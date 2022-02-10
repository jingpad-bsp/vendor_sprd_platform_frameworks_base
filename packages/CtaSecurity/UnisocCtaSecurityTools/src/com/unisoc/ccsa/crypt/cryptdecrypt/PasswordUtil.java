package com.unisoc.ccsa.crypt.cryptdecrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    private static final int KEY_LENGTH = 20;

    public static String getKeyWithRightLength(final String key, int keyLength) {
        if (keyLength > 0) {
            if (key.length() == keyLength) {
                return key;
            } else {
                MessageDigest md;
                try {
                    md = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    return "";
                }
                md.update(key.getBytes());
                byte[] hash = md.digest();
                if (keyLength > KEY_LENGTH) {
                    byte nhash[] = new byte[keyLength];
                    for (int i = 0; i < keyLength; i++) {
                        nhash[i] = hash[i % KEY_LENGTH];
                    }
                    hash = nhash;
                }
                return new String(hash).substring(0, keyLength);
            }
        } else {
            return key;
        }
    }
}
