package com.unisoc.ccsa.crypt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.unisoc.ccsa.R;

public class CreateCryptBoxDialog extends Dialog {

    public CreateCryptBoxDialog(Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();

        LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes((WindowManager.LayoutParams) layoutParams);
    }

    public static class Builder {
        private Context context;
        private String title;
        private EditText editTextName;
        private EditText editTextPassword;
        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getName() {
            return editTextName.getText().toString();
        }

        public String getPassword() {
            return editTextPassword.getText().toString();
        }

        public Builder setPositiveButton(DialogInterface.OnClickListener listener) {
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(DialogInterface.OnClickListener listener) {
            this.negativeButtonClickListener = listener;
            return this;
        }

        public CreateCryptBoxDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CreateCryptBoxDialog dialog = new CreateCryptBoxDialog(context);
            View layout = inflater.inflate(R.layout.cb_dialog_create_crypt_box, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            if (title != null && title.length() != 0) {
                ((TextView) layout.findViewById(R.id.title)).setText(title);
            }
            editTextName = (EditText) layout.findViewById(R.id.crypt_box_name);
            editTextPassword = (EditText) layout.findViewById(R.id.crypt_box_password);
            if (positiveButtonClickListener != null) {
                layout.findViewById(R.id.positiveButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }
            if (negativeButtonClickListener != null) {
                layout.findViewById(R.id.negativeButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
            dialog.setContentView(layout);
            return dialog;
        }
    }
}
