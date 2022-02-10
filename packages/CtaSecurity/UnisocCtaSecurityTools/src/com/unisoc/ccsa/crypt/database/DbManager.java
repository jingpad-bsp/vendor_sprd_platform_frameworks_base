package com.unisoc.ccsa.crypt.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private SQLiteDatabase db;

    public DbManager(Context context) {
        DbOpenHelper helper = new DbOpenHelper(context);

        db = helper.getWritableDatabase();
    }

    public void addFileBase(FileBase fileBase) {
        db.beginTransaction();
        db.execSQL("INSERT INTO file VALUES(null, ?, ?)", new Object[]{fileBase.privatename, fileBase.privateinfo});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addFileBaseList(List<FileBase> fileBases) {
        db.beginTransaction();
        try {
            for (FileBase fileBase : fileBases) {
                db.execSQL("INSERT INTO file VALUES(null, ?, ?)", new Object[]{fileBase.privatename, fileBase.privateinfo});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void updateInfo(FileBase fileBase) {
        ContentValues cv = new ContentValues();
        cv.put("privateinfo", fileBase.privateinfo);
        db.update("file", cv, "privatename = ?", new String[]{fileBase.privatename});
    }

    public void deleteFileBase(FileBase fileBase) {
        db.delete("file", "privatename = ?", new String[]{fileBase.privatename});
    }

    public List<FileBase> query() {
        ArrayList<FileBase> fileBaseArrayList = new ArrayList<FileBase>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            FileBase fileBase = new FileBase();
            fileBase._id = c.getInt(c.getColumnIndex("_id"));
            fileBase.privatename = c.getString(c.getColumnIndex("privatename"));
            fileBase.privateinfo = c.getString(c.getColumnIndex("privateinfo"));
            fileBaseArrayList.add(fileBase);
        }
        c.close();
        return fileBaseArrayList;
    }

    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM file", null);
        return c;
    }

    public void closeDB() {
        db.close();
    }
}
