/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.example.android.wearable.synchronizednotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.example.android.wearable.synchronizednotifications.common.Constants;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * A {@link com.google.android.gms.wearable.WearableListenerService} that will be invoked when a
 * DataItem is added or deleted. The creation of a new DataItem will be interpreted as a request to
 * create a new notification and the removal of that DataItem is interpreted as a request to
 * dismiss that notification.
 */
public class NotificationUpdateService extends WearableListenerService {

    private static final String TAG = "NotificationUpdate";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged, new version");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String content = dataMap.getString(Constants.KEY_CONTENT);
                String title = dataMap.getString(Constants.KEY_TITLE);
                if (Constants.WATCH_ONLY_PATH.equals(dataEvent.getDataItem().getUri().getPath())) {
                    buildWearableOnlyNotification(title, content, false);
                } else if (Constants.BOTH_PATH.equals(dataEvent.getDataItem().getUri().getPath())) {
                    buildWearableOnlyNotification(title, content, true);
                }
            } else if (dataEvent.getType() == DataEvent.TYPE_DELETED) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "DataItem deleted: " + dataEvent.getDataItem().getUri().getPath());
                }
                if (Constants.BOTH_PATH.equals(dataEvent.getDataItem().getUri().getPath())) {
                    // Dismiss the corresponding notification
                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                            .cancel(Constants.BOTH_ID);
                }
            }
        }
    }

    /**
     * Builds a simple notification on the wearable.
     */
    private void buildWearableOnlyNotification(String title, String content,
            boolean withDismissal) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(content);

        if (withDismissal) {
            Intent dismissIntent = new Intent(Constants.ACTION_DISMISS);
            dismissIntent.putExtra(Constants.KEY_NOTIFICATION_ID, Constants.BOTH_ID);
            PendingIntent pendingIntent = PendingIntent
                    .getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setDeleteIntent(pendingIntent);
        }

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(Constants.WATCH_ONLY_ID, builder.build());
    }

}