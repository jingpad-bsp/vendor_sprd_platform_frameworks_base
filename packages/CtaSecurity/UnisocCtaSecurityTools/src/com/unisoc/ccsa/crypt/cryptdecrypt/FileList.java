package com.unisoc.ccsa.crypt.cryptdecrypt;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


public class FileList {

    private Vector<File> vecFiles = new Vector<File>();

    public FileList(String files) {
        this(files, ":");
    }

    public FileList(String files, String separator) {
        parse(files, separator);
    }

    public FileList(File[] files) {
        for (File f : files) {
            vecFiles.add(f);
        }
    }

    public FileList(File file) {
        vecFiles.add(file);
    }

    public FileList(List list) {
        for (int i = 0; i < list.size(); i++) {
            vecFiles.add(new File((String) list.get(i)));
        }
    }

    private void parse(String files, String separator) {
        StringTokenizer st = new StringTokenizer(files, separator, false);
        while (st.hasMoreTokens()) {
            String file = st.nextToken();
            vecFiles.add(new File(file));
        }
    }

    public Iterator getIterator() {
        return vecFiles.iterator();
    }

    public File get(int pos) {
        if (pos > vecFiles.size()) {
            return null;
        } else {
            return vecFiles.get(pos);
        }
    }

    public int size() {
        return vecFiles.size();
    }

    public void add(File f) {
        vecFiles.add(f);
    }
}
