package com.unisoc.ccsa.crypt;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.Manifest;

import com.unisoc.ccsa.Log;
import com.unisoc.ccsa.R;
import java.io.File;

public class CryptBoxFragment extends Fragment {

    public static final String TAG = "CryptBoxFragment";
    private static CryptBoxFragment mCryptBoxFragment = null;
    public static final int PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 100;
    private Button mCreateCryptBox = null;
    private Button mButton = null;

    public CryptBoxFragment() {
        // Required empty public constructor
    }

    public static CryptBoxFragment getInstance() {
        if (mCryptBoxFragment == null) {
            mCryptBoxFragment = new CryptBoxFragment();
        }
        return mCryptBoxFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crypt_box, container, false);
        mButton = view.findViewById(R.id.buttonid);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ConfirmPasswordDialog.Builder builder = new ConfirmPasswordDialog.Builder(getActivity());
                builder.setPositiveButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmCryptBoxPassword(builder.getPassword());
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                builder.create().show();

            }
        });
        mButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DeleteConfirmDialog();
                return true;
            }
        });

        mCreateCryptBox = view.findViewById(R.id.create_crypt_box);
        mCreateCryptBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CreateCryptBoxDialog.Builder builder = new CreateCryptBoxDialog.Builder(getActivity());
                builder.setPositiveButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createCryptBox(builder.getName(), builder.getPassword());
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        checkPermissions();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initCheckBoxView();
    }

    private void initCheckBoxView() {
        if (CheckCryptBoxExist()) {
            SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("SharedPreferences", 0);
            String name = mSharedPreferences.getString("name", "");
            String text = getResources().getString(R.string.crypt_box) + ":" + name;
            mButton.setText(text);
            mButton.setVisibility(View.VISIBLE);
            mCreateCryptBox.setVisibility(View.GONE);
        } else {
            mButton.setVisibility(View.GONE);
            mCreateCryptBox.setVisibility(View.VISIBLE);
        }
    }

    private boolean CheckCryptBoxExist() {
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("SharedPreferences", 0);
        String mName = mSharedPreferences.getString("name", "");
        String mPassword = mSharedPreferences.getString("password", "");
        return mName.length() > 0 && mPassword.length() > 0;
    }

    private void confirmCryptBoxPassword(String password) {
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("SharedPreferences", 0);
        String mName = mSharedPreferences.getString("name", "");
        String mPassword = mSharedPreferences.getString("password", "");
        if (password != null && password.length() > 0 && mPassword.length() > 0 && mPassword.equals(password)) {
            if (mName.length() > 0) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), CryptBoxActivity.class);
                intent.putExtra("cryptbox_name", mName);
                intent.putExtra("cryptbox_password", password);
                startActivity(intent);
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
        }
    }

    private void createCryptBox(String name, String password) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.invalid_name), Toast.LENGTH_LONG).show();
            return;
        }
        if (password == null || password.length() <= 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.invalid_password), Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "createCryptBox name=" + name + ",password=" + password);
        File file = new File(Environment.getExternalStorageDirectory() + "/" + CryptBoxActivity.DIRECTORY_HIDE + name);
        if (!file.exists() || !file.isDirectory()) {
            if (!file.mkdirs()) {
                Toast.makeText(getActivity(), getResources().getString(R.string.mkdir_failed), Toast.LENGTH_LONG).show();
            }
        }
        storeData(name, password);
        initCheckBoxView();
    }

    protected void DeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.confirm_delete_cryptbox));
        builder.setTitle(getResources().getString(R.string.warning));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCryptBox();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deleteCryptBox() {
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("SharedPreferences", 0);
        mSharedPreferences.edit().clear().apply();
        initCheckBoxView();
    }

    private void storeData(String name, String password) {
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("SharedPreferences", 0);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("name", name);
        mEditor.putString("password", password);
        mEditor.apply();
    }

    protected void checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "no storage permission");
            getActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean resultAllGranted = true;
        if (grantResults.length > 0) {
            for (int result : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != result) {
                    resultAllGranted = false;
                    break;
                }
            }
        } else {
            resultAllGranted = false;
        }
        if (requestCode == PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE) {
            if (!resultAllGranted) {
                getActivity().finish();
            }
        }
    }
}
