package com.udacity.project4.utils

import androidx.core.app.JobIntentService
import com.udacity.project4.locationreminders.data.ReminderDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

abstract class CoroutineAwareJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    val remindersLocalRepository: ReminderDataSource by inject()
}