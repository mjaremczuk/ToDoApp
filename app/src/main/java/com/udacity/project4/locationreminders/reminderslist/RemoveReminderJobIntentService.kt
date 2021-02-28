package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.content.Intent
import com.udacity.project4.locationreminders.reminderslist.RemoveReminderBroadcastReceiver.Companion.EXTRA_NOTIFICATION_ID
import com.udacity.project4.locationreminders.reminderslist.RemoveReminderBroadcastReceiver.Companion.EXTRA_REMINDER_ID
import com.udacity.project4.utils.CoroutineAwareJobIntentService
import com.udacity.project4.utils.cancelNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RemoveReminderJobIntentService : CoroutineAwareJobIntentService() {

    companion object {
        private const val JOB_ID = 574

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                RemoveReminderJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        if (intent.action == RemoveReminderBroadcastReceiver.REMOVE_REMINDER_ACTION) {
            cancelNotification(this, intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
            val id = intent.getStringExtra(EXTRA_REMINDER_ID)
            id?.let {
                CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                    remindersLocalRepository.delete(it)
                }
            }
        }
    }
}