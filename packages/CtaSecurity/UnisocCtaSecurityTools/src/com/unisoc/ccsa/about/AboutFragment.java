package com.unisoc.ccsa.about;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unisoc.ccsa.R;


public class AboutFragment extends Fragment {

    private static AboutFragment mAboutFragment = null;

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment getInstance() {
        if (mAboutFragment == null) {
            mAboutFragment = new AboutFragment();
        }
        return mAboutFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        TextView textView = view.findViewById(R.id.app_version);
        textView.setText(getVersion(getActivity()));
        return view;
    }

    private String getVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = "";
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }
}
