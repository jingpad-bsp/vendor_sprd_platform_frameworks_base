package com.unisoc.ccsa.crypt.cryptdecrypt;

import java.io.IOException;
import java.io.OutputStream;


public class BlockCipherOutputStream extends OutputStream {
    private Algorithm alg;
    private OutputStream os;

    public BlockCipherOutputStream(OutputStream os, Algorithm alg) {
        this.os = os;
        this.alg = alg;
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(alg.encryptBytes(b), off, len);
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException(
                "BlockCipherOutputStream does not support single byte operations.");
    }
}
