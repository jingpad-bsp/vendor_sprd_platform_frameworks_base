package com.unisoc.ccsa.crypt.cryptdecrypt;

import java.io.InputStream;
import java.io.OutputStream;

public interface Algorithm {
    boolean initEncrypt(String password);

    boolean initDecrypt(String password);

    byte[] encryptBytes(byte[] input);

    byte[] decryptBytes(byte[] input);

    InputStream getEncryptionStream(InputStream in);

    OutputStream getDecryptionStream(OutputStream out);

    EncryptionMode[] getEncryptionMode();

    String getName();

    int getKeyLength();

    String getLicense();

    int getBlockSize();

    String getWebsite();

    String getSuffix();

    String getAuthor();
}
