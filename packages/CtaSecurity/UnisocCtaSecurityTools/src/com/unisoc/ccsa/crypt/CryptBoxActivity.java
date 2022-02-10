package com.unisoc.ccsa.crypt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.unisoc.ccsa.Log;
import com.unisoc.ccsa.R;
import com.unisoc.ccsa.crypt.cryptdecrypt.Model;
import com.unisoc.ccsa.crypt.database.DbManager;
import com.unisoc.ccsa.crypt.database.FileBase;

import java.io.File;


public class CryptBoxActivity extends AppCompatActivity {
    public final static String TAG = "CryptBoxActivity";

    public final static String DIRECTORY_HIDE = ".";
    public final static String MIME_TYPE_ALL = "*/*";
    public final static int REQUEST_CODE_GET_FILE = 100;

    public final static int ENCRYPT_FILE_DONE = 201;
    public final static int DECRYPT_FILE_DONE = 202;

    public final static int MEDIAPROVIDER_DELETE = 1;
    public final static int MEDIAPROVIDER_ADD = 2;

    private DbManager mDbManager;
    private Cursor mCursor;
    private SimpleCursorAdapter mAdapter;
    private String mPassword;
    private String mCryptDictory;
    private Handler mUIHandler;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cb_activity_crypt_box);

        Intent intent = getIntent();
        mPassword = intent.getStringExtra("cryptbox_password");
        mCryptDictory = Environment.getExternalStorageDirectory() + "/" + DIRECTORY_HIDE + intent.getStringExtra("cryptbox_name");
        Log.d(TAG, "mPassword=" + mPassword + ",mCryptDictory=" + mCryptDictory);

        Button mAddFile = (Button) findViewById(R.id.add_crypt_file);
        mAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(MIME_TYPE_ALL);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivityForResult(intent, REQUEST_CODE_GET_FILE);
            }
        });
        startHandler();
        initListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
    }

    @Override
    protected void onDestroy() {
        mDbManager.closeDB();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode=" + requestCode + ",resultCode=" + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_GET_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d(TAG, "URI=" + uri);
                    String filePath = UriToFile.getPath(this, uri);
                    if (filePath == null) {
                        Log.e(TAG, "filePath is null!");
                        return;
                    }
                    Log.d(TAG, "filePath=" + filePath);
                    cryptFile(filePath);
                }
                break;
            case REQUEST_CODE_GET_PATH:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    mDecryptPathStr = bundle.getString("absolutepath");
                    Log.d(TAG, "mDecryptPathStr=" + mDecryptPathStr);
                    if (mDecryptPath != null && mDecryptPathStr != null) {
                        mDecryptPath.setText(mDecryptPathStr);
                    }
                }
                break;
        }
    }

    private void startHandler() {
        mUIHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ENCRYPT_FILE_DONE:
                        Log.d(TAG, "ENCRYPT_FILE_DONE");
                        File mInputFile = new File((String) msg.obj);
                        FileBase fileBase = new FileBase(mInputFile.getName(), mCryptDictory);
                        addOne(fileBase);
                        updateListView();
                        updateMediaProvider(MEDIAPROVIDER_DELETE, mInputFile);
                        mProgressDialog.dismiss();
                        break;
                    case DECRYPT_FILE_DONE:
                        Log.d(TAG, "DECRYPT_FILE_DONE");
                        File mOutputFile = new File((String) msg.obj);
                        deleteOne(mOutputFile.getName());
                        updateListView();
                        updateMediaProvider(MEDIAPROVIDER_ADD, mOutputFile);
                        mProgressDialog.dismiss();
                        break;
                    default:
                        Log.d(TAG, "nothing todo");
                }
            }
        };
    }

    private void cryptFile(String file) {
        Model mModle = new Model(mUIHandler);
        mModle.setInputFile(true, file);
        mModle.setPassword(mPassword);
        mModle.setEncryptOutputDirectory(mCryptDictory);
        mModle.encrypt();
        createProgressDialog(getResources().getString(R.string.process_crypt));
    }

    private void decryptFile(String file, String outputDirectory) {
        Model mModle = new Model(mUIHandler);
        mModle.setInputFile(false, file);
        mModle.setPassword(mPassword);
        mModle.setOutputDirectory(outputDirectory);
        mModle.decrypt();
        createProgressDialog(getResources().getString(R.string.process_decrypt));
    }

    private void createProgressDialog(String msg) {
        if (mProgressDialog != null) {
            mProgressDialog = null;
        }
        mProgressDialog = ProgressDialog.show(this, msg, getResources().getString(R.string.process_wait));
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "mProgressDialog onDismiss!");
                mProgressDialog = null;
            }
        });
    }

    private void initListView() {
        ListView mListView = (ListView) findViewById(R.id.listview);
        mDbManager = new DbManager(this);
        mCursor = mDbManager.queryTheCursor();
        startManagingCursor(mCursor);
        mAdapter = new SimpleCursorAdapter(this, R.layout.cb_list_item, mCursor,
                new String[]{FileBase.NAME, FileBase.INFO},
                new int[]{R.id.itemname, R.id.itemsummary});
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor mTempCursor = (Cursor) parent.getItemAtPosition(position);
                String name = mTempCursor.getString(mTempCursor.getColumnIndex(FileBase.NAME));
                String info = mTempCursor.getString(mTempCursor.getColumnIndex(FileBase.INFO));
                Log.d(TAG, "position=" + position + ",name=" + name + ",info=" + info);
                selectDecryptPath(name);
            }
        });
    }

    public void updateListView() {
        mCursor.requery();
        mAdapter.notifyDataSetChanged();
    }

    public void addOne(FileBase fileBase) {
        mDbManager.addFileBase(fileBase);
    }

    public void deleteOne(String name) {
        FileBase mFileBase = new FileBase();
        mFileBase.privatename = name;
        mDbManager.deleteFileBase(mFileBase);
    }

    private EditText mDecryptPath;
    private AlertDialog mDecryptDialog = null;
    public final static int REQUEST_CODE_GET_PATH = 101;
    public String mDecryptPathStr = Environment.getExternalStorageDirectory().toString();

    private void closemCryptDialog() {
        if (mDecryptDialog != null) {
            mDecryptDialog.dismiss();
            mDecryptDialog = null;
        }
    }

    public void selectDecryptPath(final String name) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View certificateView = factory.inflate(R.layout.cb_select_decrypt_path, null);
        mDecryptPath = (EditText) certificateView.findViewById(R.id.decrypt_path_edit);
        Button mSelectPath = (Button) certificateView.findViewById(R.id.select_path);
        Button mOK = (Button) certificateView.findViewById(R.id.select_path_ok);
        Button mCancel = (Button) certificateView.findViewById(R.id.select_path_cancel);
        mDecryptPath.setText(mDecryptPathStr);
        mSelectPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDecryptStorage();
            }
        });
        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = mCryptDictory + "/" + name;
                decryptFile(filePath, mDecryptPath.getText().toString());
                closemCryptDialog();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closemCryptDialog();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_decrypt_path));
        builder.setView(certificateView);
        if (mDecryptDialog != null) {
            closemCryptDialog();
        }
        mDecryptDialog = builder.create();
        mDecryptDialog.show();
    }

    private void selectDecryptStorage() {
        Intent intent = new Intent();
        intent.setClassName(this, "com.unisoc.ccsa.crypt.filelistview.SelectPathListActivity");
        startActivityForResult(intent, REQUEST_CODE_GET_PATH);
    }

    private void updateMediaProvider(int action, File mFile) {
        if (action == MEDIAPROVIDER_DELETE) {
            final ContentResolver resolver = getContentResolver();
            final Uri externalUri = MediaStore.Files.getContentUri("external");
            final String path = mFile.getAbsolutePath();
            resolver.delete(externalUri, "_data LIKE ?1 AND lower(_data)=lower(?2)", new String[]{path, path});
        } else if (action == MEDIAPROVIDER_ADD) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(mFile));
            sendBroadcast(intent);
        }
    }
}
