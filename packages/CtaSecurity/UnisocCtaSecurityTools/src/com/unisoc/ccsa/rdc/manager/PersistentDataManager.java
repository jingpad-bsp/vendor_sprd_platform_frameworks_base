package com.unisoc.ccsa.rdc.manager;

import android.content.SharedPreferences;

public class PersistentDataManager {
    private static PersistentDataManager passwdMgr;
    private SharedPreferences sharedP;

    private PersistentDataManager() {
    }

    public static PersistentDataManager getInstance() {
        if (passwdMgr == null) {
            passwdMgr = new PersistentDataManager();
        }

        return passwdMgr;
    }

    public void setSharedPreferences(SharedPreferences sp) {
        sharedP = sp;
    }

    public boolean setNickname(String nickname) {
        if (sharedP == null)
            return false;

        return sharedP.edit().putString("nickname", nickname).commit();
    }

    public String getNickname(String nickname) {
        if (sharedP == null)
            return null;

        return sharedP.getString("nickname", null);
    }

    public boolean bindImsi(String num, int phoneId) {
        if (sharedP == null)
            return false;

        return sharedP.edit().putString("bindingImsi" + phoneId, num).commit();
    }

    public String getBindingImsi(int phoneId) {
        if (sharedP == null)
            return null;

        return sharedP.getString("bindingImsi" + phoneId, null);
    }

    public boolean setReletivesNum(String num) {
        if (sharedP == null)
            return false;

        return sharedP.edit().putString("reletivesNum", num).commit();
    }

    public String getReletivesNum() {
        if (sharedP == null)
            return null;

        return sharedP.getString("reletivesNum", null);
    }

    public String getPassword() {
        if (sharedP == null)
            return null;
        return sharedP.getString("password", null);
    }

    public boolean setPassword(String passwd) {
        if (sharedP == null)
            return false;

        return sharedP.edit().putString("password", passwd).commit();
    }

    public boolean turnOn() {
        if (sharedP == null)
            return false;

        return sharedP.edit().putBoolean("enabled", true).commit();
    }

    public boolean turnOff() {
        if (sharedP == null)
            return false;

        return sharedP.edit().putBoolean("enabled", false).commit();
    }

    public boolean isOn() {
        if (sharedP == null)
            return false;

        return sharedP.getBoolean("enabled", false);
    }

    public boolean isAgree() {
        if (sharedP == null)
            return false;

        return sharedP.getBoolean("agree", false);
    }

    public boolean agreement(boolean agree) {
        if (sharedP == null)
            return false;

        return sharedP.edit().putBoolean("agree", true).commit();
    }

    public String getSimChangingNotedImsi(int phoneId) {
        if (sharedP == null)
            return null;

        return sharedP.getString("notedImsi" + phoneId, null);
    }

    public boolean setSimChangingNotedImsi(String num, int phoneId) {
        if (sharedP == null)
            return false;

        return sharedP.edit().putString("notedImsi" + phoneId, num).commit();
    }
}
