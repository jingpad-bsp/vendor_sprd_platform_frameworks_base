package com.unisoc.ccsa.crypt.cryptdecrypt;


public enum EncryptionMode {
    MODE_BLOCK, MODE_STREAM;

    public static EncryptionMode getBestEncryptionMode(EncryptionMode[] modes) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == MODE_STREAM) {
                return MODE_STREAM;
            } else if (modes[i] == MODE_BLOCK) {
                return MODE_BLOCK;
            }
        }
        return modes[0];
    }
}
