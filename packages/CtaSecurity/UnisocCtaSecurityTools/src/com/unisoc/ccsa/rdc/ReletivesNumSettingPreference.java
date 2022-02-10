package com.unisoc.ccsa.rdc;


import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;
import com.unisoc.ccsa.rdc.manager.SimsManager;
import com.unisoc.ccsa.rdc.service.CmdPerformerService;

public class ReletivesNumSettingPreference extends DialogPreference {
    public static final String EXTRA_ARGS = "extra_args";
    private EditText num_editor;
    private PersistentDataManager pm;
    private Context ctx;

    public ReletivesNumSettingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setDialogLayoutResource(R.layout.rdc_pref_dialog_reletives_num);
        ctx = context;
    }

    public void setPersistentDataManager(PersistentDataManager pm) {
        this.pm = pm;

        if (pm != null) {
            String num = pm.getReletivesNum();
            if (num != null && !num.equals("")) {
                this.setSummary(ctx.getResources().getString(R.string.isSet) + num);
            } else {
                this.setSummary(ctx.getResources().getString(R.string.isNotSet));
            }
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onClick() {
        // TODO Auto-generated method stub
        super.onClick();
    }

    @Override
    protected View onCreateDialogView() {
        return super.onCreateDialogView();
    }

    @Override
    protected void onBindDialogView(View root) {
        num_editor = (EditText) root.findViewById(R.id.reletives_num_edit);
        super.onBindDialogView(root);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (pm != null && !num_editor.getText().toString().equals("")) {
                pm.setReletivesNum(num_editor.getText().toString());
                int cnt = SimsManager.getInstance(ctx).getPhoneCnt();
                for (int i = 0; i < cnt; i++) {
                    PersistentDataManager.getInstance().setSimChangingNotedImsi(null, i);
                }
                ctx.startService(new Intent(ctx, CmdPerformerService.class).putExtra(
                        CmdPerformerService.EXTRA_CMD,
                        CmdPerformerService.CMD_SEND_CONFIRM_RELETIVES_MESSAGE));
            }
        }
        super.onClick(dialog, which);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (pm.getReletivesNum() != null) {
            this.setSummary(ctx.getResources().getString(R.string.isSet) + pm.getReletivesNum());
        } else {
            this.setSummary(ctx.getResources().getString(R.string.isNotSet));
        }
        super.onDialogClosed(positiveResult);
    }
}
