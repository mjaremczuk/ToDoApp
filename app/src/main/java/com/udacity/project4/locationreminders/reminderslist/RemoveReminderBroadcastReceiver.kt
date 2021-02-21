package com.udacity.project4.locationreminders.reminderslist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RemoveReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val REMOVE_REMINDER_REQUEST_CODE = 7
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val REMOVE_REMINDER_ACTION =
            "RemoveReminderBroadcastReceiver.todoapp.REMOVE_REMINDER_ACTION"

        fun createIntent(context: Context, reminderId: String, notificationId: Int): Intent {
            return Intent(context, RemoveReminderBroadcastReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                action = REMOVE_REMINDER_ACTION
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        RemoveReminderJobIntentService.enqueueWork(context, intent)
    }
}