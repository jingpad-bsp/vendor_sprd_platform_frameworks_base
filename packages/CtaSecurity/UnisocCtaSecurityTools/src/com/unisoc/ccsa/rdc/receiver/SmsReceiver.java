/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.unisoc.ccsa.rdc.receiver;

import java.util.ArrayList;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.unisoc.ccsa.rdc.service.CmdPerformerService;

public class SmsReceiver extends BroadcastReceiver {
    private final static String SUB_TAG = "SmsReceiver";
    public static final String CMD_PREFFIX = "##";
    public static final String CMD_SUFFIX = "#";
    public static final String ARG_LIMIT = "#";

    public static final String SMS_CMD_LOCK = "SP";
    public static final String SMS_CMD_DELETE = "QC";

    public static final String EXTRA_FROM_ADDRESS = CmdPerformerService.EXTRA_FROM_ADDRESS;
    public static final String EXTRA_ARGS = CmdPerformerService.EXTRA_ARGS;
    public static final String EXTRA_CMD = CmdPerformerService.EXTRA_CMD;


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras == null)
            return;

        Object[] pdus = (Object[]) extras.get("pdus");

        if (null ==  pdus)
            return;

        for (Object pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            String fromAddress = message.getOriginatingAddress();
            String body = message.getMessageBody().toString();

            if (body.isEmpty()) {
                continue;
            }


            if (!body.regionMatches(0, CMD_PREFFIX, 0, CMD_PREFFIX.length())) {
                continue;
            }


            if (!body.regionMatches(body.length() - CMD_SUFFIX.length(), CMD_SUFFIX, 0,
                    CMD_SUFFIX.length())) {
                continue;
            }

            String content = body.substring(CMD_PREFFIX.length(),
                    body.length() - CMD_SUFFIX.length());


            String[] args = content.split(ARG_LIMIT);

            // decomplex command
            Intent cmdIntent;
            if (args[0].equals(SMS_CMD_LOCK) && args.length == CmdPerformerService.ARGS_SIZE_3) {
                ArrayList<String> argsList = arrayToArrayList(args, 1, 2);
                cmdIntent = new Intent(context, CmdPerformerService.class);
                cmdIntent.putExtra(EXTRA_CMD, CmdPerformerService.CMD_FORCE_PASSWORD_AND_LOCK_SCREEN);
                cmdIntent.putExtra(EXTRA_FROM_ADDRESS, fromAddress);
                cmdIntent.putStringArrayListExtra(EXTRA_ARGS, argsList);
            } else if (args[0].equals(SMS_CMD_DELETE) && args.length == CmdPerformerService.ARGS_SIZE_2) {
                ArrayList<String> argsList = arrayToArrayList(args, 1, 1);
                cmdIntent = new Intent(context, CmdPerformerService.class);
                cmdIntent.putExtra(EXTRA_CMD, CmdPerformerService.CMD_WIPE_DATA);
                cmdIntent.putExtra(EXTRA_FROM_ADDRESS, fromAddress);
                cmdIntent.putStringArrayListExtra(EXTRA_ARGS, argsList);
            } else {
                continue;
            }

            context.startService(cmdIntent);
            abortBroadcast();
        }
    }

    private ArrayList<String> arrayToArrayList(String[] args, int start, int end) {
        ArrayList<String> argsList = new ArrayList<String>();
        for (int i = start; i <= end; i++) {
            argsList.add(args[i]);
        }

        return argsList;
    }
}
