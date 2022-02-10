package com.unisoc.ccsa.rdc;


import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.unisoc.ccsa.Log;
import com.unisoc.ccsa.R;

public class GuideDialogActivity extends Activity {

    private static final String TAG = "GuideDialogActivity";
    public static final int TYPE_LOCK_INFO = 1;
    public static final int TYPE_DELETE_INFO = 2;
    public static final int TYPE_RELATIVES_INFO = 3;

    private TextView instru;

    @Override
    protected void onResume() {
        if (this.getIntent() != null) {
            int type = this.getIntent().getIntExtra("type", 0);
            if (type == TYPE_LOCK_INFO) {
                instru.setText(R.string.lock_info);
            } else if (type == TYPE_DELETE_INFO) {
                instru.setText(R.string.delete_info);
            } else if (type == TYPE_RELATIVES_INFO) {
                instru.setText(R.string.relatives_info);
            } else {
                Log.e(TAG, "invalid type");
            }
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instru = new TextView(this);
        this.addContentView(instru, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        super.onCreate(savedInstanceState);
    }

    protected void onSaveInstanceState(Bundle outState) {
    }

}
