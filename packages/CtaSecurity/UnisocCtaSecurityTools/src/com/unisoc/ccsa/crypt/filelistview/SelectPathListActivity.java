package com.unisoc.ccsa.crypt.filelistview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import com.unisoc.ccsa.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.unisoc.ccsa.R;

import java.io.File;

public class SelectPathListActivity extends AppCompatActivity {

    public static final String TAG = "SelectPathListActivity";
    private static final int MENU_ID_PATH_SAVE = Menu.FIRST;
    private static final int MENU_ID_PATH_PROP = Menu.FIRST + 1;
    private String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
    private File mCurrentDir = new File(externalStoragePath);
    private TextView mTitleText;
    boolean fileDisplay = false;
    private FileListAdapter mOnlyAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.cb_activity_select_path_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            Log.i(TAG, "actionBar is null");
            finish();
        }
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View customTitle = getLayoutInflater().inflate(R.layout.cb_custom_title, null);
        actionBar.setCustomView(customTitle);
        mTitleText = (TextView) customTitle.findViewById(R.id.custom_title);
        initListView();
        updateTitle();
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.filelist);
        mOnlyAdapter = new FileListAdapter(this);
        if (fileDisplay) {
            mOnlyAdapter.sortImpl(new File(externalStoragePath), "*");
        } else {
            mOnlyAdapter.sortImpl(new File(externalStoragePath), "0");
        }
        mListView.setAdapter(mOnlyAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = ((FileListAdapter.ViewHolder) view.getTag()).filename.getText().toString();
                final File file = new File(mCurrentDir, itemStr);
                if (file.isDirectory()) {
                    mCurrentDir = new File(mCurrentDir.getPath() + "/" + itemStr);
                    updateListView();
                    mListView.setSelection(0);
                } else {
                    if (fileDisplay) {
                        Uri uri = Uri.parse("file:" + file.getPath());
                        Intent intent = new Intent();
                        intent.setDataAndType(uri, getFileType(file));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });
    }

    public void updateTitle() {
        mTitleText.setText(mCurrentDir.getPath());
    }

    public void updateListView() {
        if (fileDisplay) {
            mOnlyAdapter.sortImpl(mCurrentDir, "*");
        } else {
            mOnlyAdapter.sortImpl(mCurrentDir, "0");
        }
        updateTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_PATH_SAVE: {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("absolutepath", mCurrentDir.getAbsolutePath() + "/");
                Log.i(TAG, "MENU_ID_PATH_SAVE " + mCurrentDir.getAbsolutePath());
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
            case MENU_ID_PATH_PROP: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ID_PATH_SAVE, 0, getResources().getString(R.string.path_save)).setIcon(
                android.R.drawable.ic_menu_add);
        menu.add(0, MENU_ID_PATH_PROP, 0, getResources().getString(R.string.cancel)).setIcon(
                android.R.drawable.ic_menu_add);
        return true;
    }

    public static String getFileType(File file) {
        String str = getFileExtendName(file.getPath());
        if (str == null) {
            return "filetype/null";
        }
        // ------------------------------------------
        if (str.equalsIgnoreCase("ppt") || str.equalsIgnoreCase("pps"))
            return "application/powerpoint";
        else if (str.equalsIgnoreCase("doc"))
            return "application/msword";
        else if (str.equalsIgnoreCase("pdf"))
            return "application/pdf";
        else if (str.equalsIgnoreCase("xls"))
            return "application/excel";
            // ------------------------------------------
        else if (str.equalsIgnoreCase("jpg") || str.equalsIgnoreCase("jpeg"))
            return "image/jpeg";
        else if (str.equalsIgnoreCase("bmp"))
            return "image/x-ms-bmp";
        else if (str.equalsIgnoreCase("wbmp"))
            return "image/vnd.wap.wbmp";
        else if (str.equalsIgnoreCase("png"))
            return "image/png";
        else if (str.equalsIgnoreCase("gif"))
            return "image/gif";
            // ------------------------------------------
        else if (str.equalsIgnoreCase("avi"))
            return "video/x-msvideo";
        else if (str.equalsIgnoreCase("wmv"))
            return "video/x-ms-wmv";
        else if (str.equalsIgnoreCase("mp4"))
            return "video/mp4";
        else if (str.equalsIgnoreCase("3gp"))
            return "video/3gpp";
        else if (str.equalsIgnoreCase("3g2"))
            return "video/3gpp2";
            // ------------------------------------------
        else if (str.equalsIgnoreCase("mp3"))
            return "audio/mpeg";
        else if (str.equalsIgnoreCase("amr"))
            return "audio/amr";
        else if (str.equalsIgnoreCase("mid") || str.equalsIgnoreCase("midi"))
            return "audio/midi";
        else if (str.equalsIgnoreCase("aac"))
            return "audio/aac";
        else if (str.equalsIgnoreCase("wav"))
            return "audio/x-wav";
        else if (str.equalsIgnoreCase("m4a"))
            return "audio/x-m4a";
        else if (str.equalsIgnoreCase("ogg"))
            return "audio/ogg";
        else if (str.equalsIgnoreCase("wma"))
            return "audio/wma";
        else if (str.equalsIgnoreCase("wmv"))
            return "video/wmv";
        else if (str.equalsIgnoreCase("asf"))
            return "video/asf";
        else if (str.equalsIgnoreCase("flv"))
            return "video/flv";
        else if (str.equalsIgnoreCase("flac"))
            return "audio/flac";
        else if (str.equalsIgnoreCase("ape"))
            return "audio/ape";
            // ------------------------------------------
        else if (str.equalsIgnoreCase("vcf"))
            return "text/x-vCard";
        else if (str.equalsIgnoreCase("vcs"))
            return "text/x-vCalendar";
        else if (str.equalsIgnoreCase("txt"))
            return "text/plain";
        else
            return "filetype/other";
    }

    public static String getFileExtendName(String filename) {
        int index = filename.lastIndexOf('.');
        return index == -1 ? null : filename.substring(index + 1);
    }


}
