package com.udacity.project4.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemoveReminderBroadcastReceiver

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, reminderDataItem: ReminderDataItem) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notificationId = getUniqueId()
    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    val intent = ReminderDescriptionActivity.newIntent(context.applicationContext, reminderDataItem)

    //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(ReminderDescriptionActivity::class.java)
        .addNextIntent(intent)
    val notificationPendingIntent = stackBuilder
        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

    val removeIntent = PendingIntent.getBroadcast(
        context,
        RemoveReminderBroadcastReceiver.REMOVE_REMINDER_REQUEST_CODE,
        RemoveReminderBroadcastReceiver.createIntent(context, reminderDataItem.id, notificationId),
        0
    )
//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(reminderDataItem.title)
        .setContentText(reminderDataItem.location)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_input_delete,
                context.getString(R.string.delete_reminder),
                removeIntent
            )
        )
        .build()

    notificationManager.notify(notificationId, notification)
}

fun cancelNotification(context: Context, id: Int) =
    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(id)

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())