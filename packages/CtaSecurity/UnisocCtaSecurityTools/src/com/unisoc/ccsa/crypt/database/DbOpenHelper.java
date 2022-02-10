package com.unisoc.ccsa.crypt.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "filelist.db";
    public static final String DATABASE_TABLE_FILE = "file";
    public static final String DATABASE_TABLE_FILE_PRIVATENAME = "privatename";
    public static final String DATABASE_TABLE_FILE_PRIVATEINFO = "privateinfo";
    private static final int DATABASE_VERSION = 1;

    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_FILE
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATABASE_TABLE_FILE_PRIVATENAME + " VARCHAR, "
                + DATABASE_TABLE_FILE_PRIVATEINFO + " TEXT)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
        db.execSQL("ALTER TABLE file ADD COLUMN other STRING");
    }
}
