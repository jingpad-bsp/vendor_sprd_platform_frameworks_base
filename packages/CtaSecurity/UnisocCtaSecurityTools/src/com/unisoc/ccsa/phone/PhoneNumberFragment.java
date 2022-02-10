package com.unisoc.ccsa.phone;


import android.os.Bundle;
import android.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unisoc.ccsa.R;

import static android.content.Context.TELEPHONY_SERVICE;


public class PhoneNumberFragment extends Fragment {

    private static PhoneNumberFragment mPhoneNumberFragment = null;

    public PhoneNumberFragment() {
        // Required empty public constructor
    }

    public static PhoneNumberFragment getInstance() {
        if (mPhoneNumberFragment == null) {
            mPhoneNumberFragment = new PhoneNumberFragment();
        }
        return mPhoneNumberFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_phone_number, container, false);

        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);

        String number = null;
        try {
            number = telephonyManager.getLine1Number();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        TextView textView = view.findViewById(R.id.phone_number);
        if (TextUtils.isEmpty(number)) {
            textView.setText(getString(R.string.cantGetNum));
        } else {
            textView.setText(getString(R.string.phone_numner, number));
        }
        return view;
    }
}
