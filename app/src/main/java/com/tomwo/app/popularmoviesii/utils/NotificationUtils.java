package com.tomwo.app.popularmoviesii.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.tomwo.app.popularmoviesii.ui.MainActivity;
import com.tomwo.app.popularmoviesii.R;

/**
 * Created by wooldridgetm on 6/2/17.
 */
public class NotificationUtils
{
    // pendingIntentId to reference the pending intent (unique)
    private static final int PENDING_INTENT_ID_ALL_LOCAL_MOVIES = 7062;
    private static final int NOTIFICATION_ID_ALL_LOCAL_MOVIES = 3201;

    public static void notify(Context context)
    {
        String notificationTitle = context.getString(R.string.app_name);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_new_movies_notificatioin)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_local_movies_black_24dp))
                .setContentTitle(notificationTitle)
                .setContentText("New Movies Downloaded!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("New Movies Downloaded!"))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            builder.setPriority(Notification.PRIORITY_LOW);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // NOTIFICATION_ID allows you to update or cancel the notification later on...
        notificationManager.notify(NOTIFICATION_ID_ALL_LOCAL_MOVIES, builder.build());
    }

    public static PendingIntent contentIntent(Context context)
    {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                PENDING_INTENT_ID_ALL_LOCAL_MOVIES,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
