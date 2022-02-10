package com.unisoc.ccsa.rdc;

import com.unisoc.ccsa.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class PasswdSettingDialog {
    private static final int SHOW_SLEEP_TIME = 300;
    private AlertDialog dialog;
    private Context ctx;

    private AlertDialog.Builder builder;
    private View root;
    private IPassword passwdProcedure;
    private ICancel cancelProcedure;
    private TextView warningText;
    private TextView warningRelativesText;

    private boolean changePW = false;

    public interface ICancel {
        public boolean doCancel();
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public void setDialog(AlertDialog dialog) {
        this.dialog = dialog;
    }

    public AlertDialog.Builder getBuilder() {
        return builder;
    }

    public void setBuilder(AlertDialog.Builder builder) {
        this.builder = builder;
    }

    public interface IPassword {
        void doSave(String passwd);

        void doSaveRelativesNum(String num);

        boolean doVerify(String passwd);
    }

    public abstract class SavePassword implements IPassword, ICancel {
        SavePassword() {
        }

        @Override
        public void doSaveRelativesNum(String num) {
        }

        @Override
        public void doSave(String passwd) {
        }

        public boolean doVerify(String passwd) {
            return true;
        }

        @Override
        public boolean doCancel() {
            return true;
        }
    }

    private void removeCache() {
        if (root != null) {
            EditText tmp = (EditText) root.findViewById(R.id.old_passwd_edit);
            if (tmp != null) {
                tmp.setText("");
            }

            tmp = (EditText) root.findViewById(R.id.new_passwd_edit);
            if (tmp != null) {
                tmp.setText("");
            }

            tmp = (EditText) root.findViewById(R.id.confirm_passwd_edit);
            if (tmp != null) {
                tmp.setText("");
            }
        }
    }

    private OnClickListener l = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cancel) {
                if (dialog != null) {
                    removeCache();
                    if (cancelProcedure != null) {
                        cancelProcedure.doCancel();
                    }
                    dialog.dismiss();
                }
            } else if (v.getId() == R.id.save) {
                warningGo();
                if (passwdProcedure != null) {
                    EditText newPasswd = (EditText) root.findViewById(R.id.new_passwd_edit);


                    if (changePW) {
                        if (root == null) {
                            showWarning(ctx.getResources().getString(R.string.app_error));
                            return;
                        }

                        if (!passwdProcedure.doVerify(((EditText) root
                                .findViewById(R.id.old_passwd_edit)).getText().toString())) {
                            showWarning(ctx.getResources().getString(R.string.pwNotMatch));
                            return;
                        }
                    }

                    if (newPasswd.getText().toString().equals("")) {
                        showWarning(ctx.getResources().getString(R.string.pwCantNull));
                        return;
                    }


                    if (!passwdVerify()) {
                        showWarning(ctx.getResources().getString(R.string.newPwNotMatch));
                        return;
                    }


                    String relativesNum = ((EditText) root
                            .findViewById(R.id.relatives_number_edit)).getText().toString();
                    if (!changePW) {
                        if (relativesNum.isEmpty()) {
                            showRelativesWarning(ctx.getResources().getString(
                                    R.string.inputRelativesNum));
                            return;
                        }
                    }

                    passwdProcedure.doSave(newPasswd.getText().toString());
                    if (!changePW) {
                        passwdProcedure.doSaveRelativesNum(relativesNum);
                    }
                    removeCache();
                    dialog.dismiss();
                }
            }

        }
    };

    private void showWarning(String text) {
        if (warningText.getVisibility() == View.VISIBLE) {
            warningText.setVisibility(View.GONE);
            try {
                Thread.sleep(SHOW_SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (text != null) {
            warningText.setText(text);
        }
        warningText.setVisibility(View.VISIBLE);
    }

    private void showRelativesWarning(String text) {
        if (warningRelativesText.getVisibility() == View.VISIBLE) {
            warningRelativesText.setVisibility(View.GONE);
            try {
                Thread.sleep(SHOW_SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (text != null) {
            warningRelativesText.setText(text);
        }
        warningRelativesText.setVisibility(View.VISIBLE);
    }

    private boolean passwdVerify() {
        if (root != null) {
            EditText confirmPasswd = (EditText) root.findViewById(R.id.confirm_passwd_edit);
            EditText newPasswd = (EditText) root.findViewById(R.id.new_passwd_edit);
            if (confirmPasswd.getText().toString().equals(newPasswd.getText().toString())) {
                return true;
            }
        }

        return false;
    }

    PasswdSettingDialog(Context ctx, boolean showOld) {
        this.ctx = ctx;
        changePW = showOld;
        builder = new AlertDialog.Builder(ctx);
        root = LayoutInflater.from(ctx)
                .inflate(R.layout.rdc_pref_dialog_passwordchange, null);
        if (showOld) {
            root.findViewById(R.id.old_passwd).setVisibility(View.VISIBLE);
            root.findViewById(R.id.relatives_number_new).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.relatives_number_new).setVisibility(View.VISIBLE);
            root.findViewById(R.id.old_passwd).setVisibility(View.GONE);
            ((TextView) root.findViewById(R.id.old_passwd_text)).setText(R.string.pw);
        }

        warningText = (TextView) root.findViewById(R.id.warning_text);
        warningText.setVisibility(View.GONE);
        warningRelativesText = (TextView) root.findViewById(R.id.warning_relatives_text);
        warningRelativesText.setVisibility(View.GONE);

        root.findViewById(R.id.save).setOnClickListener(l);
        root.findViewById(R.id.cancel).setOnClickListener(l);

        builder.setView(root);
        builder.setTitle(ctx.getResources().getString(R.string.setting));
        dialog = builder.create();
    }

    private void warningGo() {
        if (warningRelativesText != null) {
            warningRelativesText.setVisibility(View.GONE);
        }

        if (warningText != null) {
            warningText.setVisibility(View.GONE);
        }
    }

    public void setOnSavingPasswd(SavePassword handle) {
        passwdProcedure = handle;
        cancelProcedure = handle;
    }

    public boolean show() {
        if (dialog != null) {
            warningGo();
            dialog.show();
            return true;
        }

        return false;
    }

    public void dispose() {
        dialog = null;
        builder = null;
        root = null;
        passwdProcedure = null;
    }

    public void dismiss() {
        if (dialog != null) {
            warningGo();
            dialog.dismiss();
        }
    }
}
