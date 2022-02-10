package com.unisoc.ccsa.rdc;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;

public abstract class BaseSwitchPreference extends Preference {
    public static final int BUTTON_SAVE = DialogInterface.BUTTON_POSITIVE;
    public static final int BUTTON_CANCEL = DialogInterface.BUTTON_POSITIVE;

    protected boolean kitkat;
    protected boolean isChecked;
    protected Context ctx;
    protected CompoundButton cb;
    protected PersistentDataManager passwdMgr;
    private AlertDialog dialog;

    @Override
    protected void onBindView(View view) {
        if (kitkat) {
            cb = (Switch) view.findViewById(R.id.checkbox);
        } else {
            cb = (CheckBox) view.findViewById(R.id.checkbox);
        }

        this.notifyDependencyChange(!isChecked);// control whether this preference should disable its dependents
        cb.setChecked(isChecked);

        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean value) {
                if (value == isChecked) return;
                cb.setEnabled(false);

                if (value) {
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
                        cb.setChecked(isChecked);
                        cb.setEnabled(true);
                    }
                });

                dialog.show();
            }

        });

        super.onBindView(view);
    }

    public BaseSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        if (Build.VERSION.SDK_INT < 14) {
            this.setWidgetLayoutResource(R.layout.rdc_custom_checkbox);
            kitkat = false;
        } else {
            this.setWidgetLayoutResource(R.layout.rdc_custom_switch);
            kitkat = true;
        }
    }

    protected abstract AlertDialog getSpecDialog();

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
                        cb.setChecked(isChecked);
                        Toast.makeText(ctx, ctx.getResources().getString(R.string.pwWrong),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    cb.setChecked(isChecked);
                }

                cb.setEnabled(true);
            }
        };

        builder.setPositiveButton(R.string.ok, l);
        builder.setNegativeButton(R.string.cancel, l);
        dialog = builder.create();
        return dialog;
    }

    protected void turnOnOffPickProof(boolean on, String notation) {
        isChecked = on;
        this.notifyDependencyChange(!on);// control whether this preference should disable its dependents
        if (notation != null) {
            Toast.makeText(ctx, notation, Toast.LENGTH_SHORT).show();
        }
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
        if (cb != null) {
            cb.setChecked(isChecked);
        }
    }

}
