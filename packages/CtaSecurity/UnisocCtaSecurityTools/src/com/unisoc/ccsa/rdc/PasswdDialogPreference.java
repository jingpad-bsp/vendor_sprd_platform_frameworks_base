package com.unisoc.ccsa.rdc;


import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;

public class PasswdDialogPreference extends DialogPreference {
    private Context ctx;
    private PasswdSettingDialog pd;
    private PasswdSettingDialog.SavePassword savepw;
    private PersistentDataManager passwdMgr;

    public PasswdDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;

        pd = new PasswdSettingDialog(context, true);
        savepw = pd.new SavePassword() {

            @Override
            public void doSave(String passwd) {
                if (passwdMgr != null) {
                    passwdMgr.setPassword(passwd);
                }
                Toast.makeText(ctx, ctx.getResources().getString(R.string.changeSaved), Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean doVerify(String passwd) {
                if (passwdMgr == null)
                    return false;
                if (passwd.equals(passwdMgr.getPassword())) {
                    return true;
                } else {
                    return false;
                }
            }


        };

        pd.setOnSavingPasswd(savepw);
    }

    @Override
    protected void showDialog(Bundle state) {
        pd.show();
    }

    @Override
    public void onActivityDestroy() {
        if (pd != null) {
            pd.dispose();
        }
        super.onActivityDestroy();
    }

    public void setPersistentDataManager(PersistentDataManager pm) {
        passwdMgr = pm;
    }
}
