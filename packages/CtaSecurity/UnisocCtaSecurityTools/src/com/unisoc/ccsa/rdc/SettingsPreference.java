package com.unisoc.ccsa.rdc;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;

public class SettingsPreference extends Preference {

    private Context ctx;
    private PersistentDataManager pdm;
    private AlertDialog dialog;

    public SettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public void setPersistentDataManager(PersistentDataManager persistent) {
        pdm = persistent;
    }

    @Override
    protected void onClick() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        showVerifyDialog();
    }

    protected void showVerifyDialog() {
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
                    if (pdm != null
                            && pdm.getPassword() != null
                            && pdm.getPassword().equals(passwdEdit.getText().toString())) {
                        ctx.startActivity(new Intent(ctx, SettingsActivity.class));
                    } else {
                        Toast.makeText(ctx, R.string.pwWrong, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                } else {
                    dialog.dismiss();
                }
            }
        };

        builder.setPositiveButton(R.string.ok, l);
        builder.setNegativeButton(R.string.cancel, l);

        dialog = builder.create();
        dialog.show();
    }

}
