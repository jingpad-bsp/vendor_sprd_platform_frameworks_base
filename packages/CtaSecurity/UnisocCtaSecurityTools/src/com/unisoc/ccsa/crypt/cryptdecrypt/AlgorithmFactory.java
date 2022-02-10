package com.unisoc.ccsa.crypt.cryptdecrypt;


import com.unisoc.ccsa.Log;

import java.util.HashMap;

public class AlgorithmFactory {
    public final static String TAG = "AlgorithmFactory";

    private HashMap<String, Algorithm> algorithms;

    private static AlgorithmFactory defaultAlgFac = null;

    private AlgorithmFactory() {

        algorithms = new HashMap<>();

        // Add default algorithms.
        addAlgorithm("Blowfish", new JCEAlgorithm("Blowfish"));
        addAlgorithm("DES", new JCEAlgorithm("DES"));
        addAlgorithm("TripleDES", new JCEAlgorithm("TripleDES"));
        addAlgorithm("AES", new JCEAlgorithm("AES"));
        addAlgorithm("RC4", new JCEAlgorithm("RC4"));
    }

    public static AlgorithmFactory getDefaultAlgorithmFactory() {
        if (defaultAlgFac == null)
            defaultAlgFac = new AlgorithmFactory();
        return defaultAlgFac;
    }

    public void addAlgorithm(String name, Algorithm alg) {
        if (!algorithms.containsKey(name))
            algorithms.put(name, alg);
    }

    public Algorithm[] getAlgorithms() {
        Object alos[] = algorithms.values().toArray();
        Algorithm[] algs = new Algorithm[alos.length];
        for (int i = 0; i < alos.length; i++) {
            algs[i] = (Algorithm) alos[i];
        }
        return algs;
    }

    public Algorithm getAlgorithm(String alg) {
        Algorithm a = algorithms.get(alg);
        if (a == null) {
            Log.i(TAG, "getAlgorithm a = null, alg = " + alg);
        }
        return a;
    }

}
