package com.unisoc.ccsa.crypt.database;

public class FileBase {

    public final static String ID = "_id";
    public final static String NAME = "privatename";
    public final static String INFO = "privateinfo";

    public int _id;
    public String privatename;
    public String privateinfo;

    public FileBase() {
    }

    public FileBase(String name, String info) {
        this.privatename = name;
        this.privateinfo = info;
    }

}
