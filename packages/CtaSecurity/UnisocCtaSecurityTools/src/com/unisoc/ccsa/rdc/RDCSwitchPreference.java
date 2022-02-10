package com.unisoc.ccsa.rdc;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.preference.CheckBoxPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;
import com.unisoc.ccsa.rdc.manager.SimsManager;
import com.unisoc.ccsa.rdc.service.CmdPerformerService;

public class RDCSwitchPreference extends CheckBoxPreference {
    public static final int BUTTON_SAVE = DialogInterface.BUTTON_POSITIVE;
    public static final int BUTTON_CANCEL = DialogInterface.BUTTON_POSITIVE;

    private PasswdSettingDialog pd;

    private PasswdSettingDialog.SavePassword savepw;
    private Context ctx;
    public static final String SUB_TAG = "RDCSwitchPreference";

    protected boolean kitkat;
    protected boolean isChecked;
    //protected Context ctx;
    protected CompoundButton cb;
    protected PersistentDataManager passwdMgr;
    private AlertDialog dialog;

    public RDCSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;

        //this.notifyDependencyChange(!isChecked);// control whether this preference should disable its dependents
        //setChecked(isChecked);

        pd = new PasswdSettingDialog(ctx, false);

        savepw = pd.new SavePassword() {

            @Override
            public boolean doCancel() {
                setChecked(!isChecked);
                return super.doCancel();
            }

            @Override
            public void doSaveRelativesNum(String num) {
                if (passwdMgr != null) {
                    passwdMgr.setReletivesNum(num);
                    ctx.startService(new Intent(ctx, CmdPerformerService.class).putExtra(
                            CmdPerformerService.EXTRA_CMD,
                            CmdPerformerService.CMD_SEND_CONFIRM_RELETIVES_MESSAGE));
                }

                super.doSaveRelativesNum(num);
            }

            @Override
            public void doSave(String passwd) {
                if (passwdMgr != null) {
                    passwdMgr.setPassword(passwd);
                    turnOnPickProof();
                }

                super.doSave(passwd);
            }
        };

        pd.setOnSavingPasswd(savepw);
    }

    protected void turnOnOffPickProof(boolean on, String notation) {
        //super.turnOnOffPickProof(on, notation);
        isChecked = on;
        //this.notifyDependencyChange(!on);// control whether this preference should disable its dependents
        if (notation != null) {
            Toast.makeText(ctx, notation, Toast.LENGTH_SHORT).show();
        }

        if (on) {
            passwdMgr.turnOn();
            SimsManager.getInstance(ctx).saveCurrentIMSIs();
            SimsManager.getInstance(ctx).saveNotedIMSIs();
            if (passwdMgr != null && passwdMgr.getReletivesNum() != null) {
                ctx.startService(new Intent(ctx, CmdPerformerService.class).putExtra(
                        CmdPerformerService.EXTRA_CMD,
                        CmdPerformerService.CMD_SEND_CONFIRM_RELETIVES_MESSAGE));
            }
        } else {
            passwdMgr.turnOff();
        }
    }


    protected AlertDialog getSpecDialog() {
        return pd.getDialog();
    }

    @Override
    protected void onClick() {
        boolean value;

        super.onClick();
        isChecked = isChecked();

        if (isChecked) {
            if (passwdMgr.getPassword() == null) {
                dialog = getSpecDialog();
            } else {
                dialog = getVerifyDialog(true);
            }
        } else {
            dialog = getVerifyDialog(false);
        }
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                setChecked(!isChecked);
            }
        });
        dialog.show();

    }

    protected AlertDialog getVerifyDialog(final boolean on) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.confirm_pw);
        View root = LayoutInflater.from(ctx)
                .inflate(R.layout.rdc_verify_passwd_dialog, null);
        builder.setView(root);

        final EditText passwdEdit = (EditText) root.findViewById(R.id.passwd_edit);
        OnClickListener l = new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (passwdMgr != null
                            && passwdMgr.getPassword() != null
                            && passwdMgr.getPassword().equals(passwdEdit.getText().toString())) {
                        if (on) {
                            turnOnPickProof();
                        } else {
                            turnOffPickProof();
                        }
                    } else {
                        setChecked(!isChecked);
                        Toast.makeText(ctx, ctx.getResources().getString(R.string.pwWrong),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setChecked(!isChecked);
                }

            }
        };

        builder.setPositiveButton(R.string.ok, l);
        builder.setNegativeButton(R.string.cancel, l);
        dialog = builder.create();
        return dialog;
    }

    protected void turnOffPickProof() {
        turnOnOffPickProof(false, ctx.getResources().getString(R.string.isOff));
    }

    protected void turnOnPickProof() {
        turnOnOffPickProof(true, ctx.getResources().getString(R.string.isOn));
    }

    public void setPersistentDataManager(PersistentDataManager pm, boolean def) {
        passwdMgr = pm;
        isChecked = pm.isOn();
    }

}
