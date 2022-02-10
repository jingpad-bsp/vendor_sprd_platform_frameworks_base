package com.unisoc.ccsa.crypt.cryptdecrypt;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.unisoc.ccsa.Log;
import com.unisoc.ccsa.crypt.CryptBoxActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Model {
    public static final String TAG = "Model";
    private AlgorithmFactory algFactory;
    private Algorithm currentAlgorithm;
    private String password = "123456"; //default password for en-/decrypt
    private String algorithmName = "Blowfish"; // default algorithm for en-/decrypt
    private File inputFile = null;
    private File outputFile = null;
    public String encryptOutputDirectory;
    public String decryptOutputDirectory = Environment.getExternalStorageDirectory().toString();
    private byte[] buffer = new byte[1024];
    private long progLength = 0, progNow = 0;
    private int progOld = 0;
    private Thread runningThread = null;
    private Handler mUIHandler;

    private Model() {
        algFactory = AlgorithmFactory.getDefaultAlgorithmFactory();
    }

    public Model(Handler handler) {
        this();
        mUIHandler = handler;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEncryptOutputDirectory(String path) {
        encryptOutputDirectory = path;
    }

    protected Algorithm getAlgorithmByName(String algname) {
        return algFactory.getAlgorithm(algname);
    }

    protected String getPassword() {
        return password;
    }

    protected void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    protected String getAlgorithmName() {
        return algorithmName;
    }

    protected Algorithm getAlgorithm() {
        return currentAlgorithm;
    }

    protected Algorithm[] getAlgorithms() {
        return algFactory.getAlgorithms();
    }

    protected void stop() {
        if (runningThread != null && runningThread.isAlive()) {
            runningThread.interrupt();
        }
    }

    private void updateProgress() {
        double dprog = progNow / (double) progLength * 100.0;
        int prog = (int) Math.round(dprog);
        if (prog > progOld) {
            Log.i(TAG, "updateProgress=" + prog);
            progOld = prog;
        }
    }

    protected File getInputFile() {
        if (inputFile == null) {
            Log.e(TAG, "can not getInputFile.");
            return null;
        }
        return inputFile;
    }

    protected File getOutputFile(Boolean isEncrypt, String directory, String name) {
        if (isEncrypt) {
            return new File(directory + "/" + name + "." + algorithmName);
        } else {
            String newName = name.substring(0, name.length() - algorithmName.length() - 1);
            return new File(directory + "/" + newName);
        }
    }

    public void setInputFile(Boolean isEncrypt, String inputFile) {
        if (isEncrypt) {
            this.inputFile = new File(inputFile);
        } else {
            this.inputFile = new File(inputFile + "." + algorithmName);
        }
    }

    public void setOutputDirectory(String outputDirectory) {
        decryptOutputDirectory = outputDirectory;
    }

    public void encrypt() {
        doEncrypt();
    }

    public void decrypt() {
        doDecrypt();
    }

    private void doEncrypt() {
        runningThread = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "doEncrypt start!");
                Algorithm alg = algFactory.getAlgorithm(algorithmName);
                currentAlgorithm = alg;
                if (!alg.initEncrypt(password)) {
                    Log.e(TAG, "doEncrypt initEncrypt failed!");
                    return;
                }

                File inputFile = getInputFile();
                if (inputFile == null || !inputFile.exists()) {
                    Log.e(TAG, "getTargetFile is null");
                    resetModel(true);
                    return;
                }
                outputFile = getOutputFile(true, encryptOutputDirectory, inputFile.getName());
                Log.d(TAG, "inputFile=" + inputFile + ",outputFile=" + outputFile);

                try {
                    InputStream in = null;
                    OutputStream os;
                    if (EncryptionMode.getBestEncryptionMode(alg.getEncryptionMode()) == EncryptionMode.MODE_STREAM) {
                        in = alg.getEncryptionStream(new FileInputStream(inputFile));
                    } else if (EncryptionMode.getBestEncryptionMode(alg.getEncryptionMode()) == EncryptionMode.MODE_BLOCK) {
                        in = new BlockCipherInputStream(new FileInputStream(inputFile), alg);
                    }
                    if (in == null) {
                        Log.d(TAG, "can not get getEncryptionStream");
                        return;
                    }
                    os = new FileOutputStream(outputFile);
                    int result;
                    while ((result = in.read(buffer)) != -1) {
                        os.write(buffer, 0, result);
                    }
                    in.close();
                    os.close();
                    Message message = mUIHandler.obtainMessage(CryptBoxActivity.ENCRYPT_FILE_DONE, inputFile.getAbsolutePath());
                    mUIHandler.sendMessage(message);
                    if (!inputFile.delete()) {
                        Log.d(TAG, "encrypt inputFile delete failed");
                    }
                    resetModel(true);
                    Log.d(TAG, "encrypt end!");
                } catch (FileNotFoundException ex) {
                    Log.e(TAG, "FileNotFoundException ex=" + ex.getLocalizedMessage());
                } catch (IOException ex) {
                    Log.e(TAG, "IOException ex=" + ex.getLocalizedMessage());
                }
            }
        };
        runningThread.start();
    }

    private void doDecrypt() {
        runningThread = new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "doDecrypt start!");
                Algorithm alg = algFactory.getAlgorithm(algorithmName);
                currentAlgorithm = alg;
                if (!alg.initDecrypt(password)) {
                    Log.e(TAG, "initDecrypt failed");
                    return;
                }
                File inputFile = getInputFile();
                if (inputFile == null || !inputFile.exists()) {
                    Log.e(TAG, "getInputFile is null");
                    resetModel(true);
                    return;
                }
                outputFile = getOutputFile(false, decryptOutputDirectory, inputFile.getName());
                Log.d(TAG, "inputFile=" + inputFile + ",outputFile=" + outputFile);
                try {
                    InputStream in;
                    OutputStream os = null;
                    in = new FileInputStream(inputFile);
                    if (EncryptionMode.getBestEncryptionMode(alg.getEncryptionMode()) == EncryptionMode.MODE_STREAM) {
                        os = alg.getDecryptionStream(new FileOutputStream(outputFile));
                    } else if (EncryptionMode.getBestEncryptionMode(alg.getEncryptionMode()) == EncryptionMode.MODE_BLOCK) {
                        os = new BlockCipherOutputStream(new FileOutputStream(outputFile), alg);
                    }
                    int result;
                    if (os == null) {
                        Log.e(TAG, "can not getDecryptionStream");
                        return;
                    }
                    while ((result = in.read(buffer)) != -1) {
                        os.write(buffer, 0, result);
                    }
                    os.close();
                    in.close();

                    if (!inputFile.delete()) {
                        Log.d(TAG, "decrypt inputFile delete failed!");
                    }
                    Message message = mUIHandler.obtainMessage(CryptBoxActivity.DECRYPT_FILE_DONE, outputFile.getAbsolutePath());
                    mUIHandler.sendMessage(message);
                    resetModel(true);
                    Log.d(TAG, "doDecrypt done");
                } catch (FileNotFoundException ex) {
                    Log.e(TAG, "error_file_not_exist" + ex.getLocalizedMessage());
                } catch (IOException ex) {
                    Log.e(TAG, "error_generic_io" + ex.getLocalizedMessage());
                }
                resetModel(true);
            }
        };
        runningThread.start();
    }

    private void resetModel(boolean fully) {
        progOld = 0;
        progLength = 0;
        progNow = 0;
        if (fully) {
            inputFile = null;
            outputFile = null;
            runningThread = null;
        }
    }

    @Override
    public String toString() {
        String rtr = "";
        rtr += "password: " + password + "\n";
        rtr += "algorithm: " + algorithmName + "\n";
        rtr += "inputFile: \n";
        if (inputFile != null)
            rtr += inputFile.getAbsolutePath();
        return rtr;
    }
}