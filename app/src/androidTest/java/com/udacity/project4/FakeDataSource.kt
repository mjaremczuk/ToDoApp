package com.udacity.project4

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var remindersData: LinkedHashMap<String?, List<ReminderDTO>> = LinkedHashMap()


    fun addReminder(vararg reminder: ReminderDTO) {
        reminder.forEach {
            remindersData.put(it.userId, remindersData[it.userId]?.plus(it) ?: listOf(it))
        }
    }

    override suspend fun getReminders(userId: String?): Result<List<ReminderDTO>> {
        remindersData[userId]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not get reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersData[reminder.userId] =
            remindersData[reminder.userId]?.plus(reminder) ?: listOf(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return remindersData.values.flatten().firstOrNull { it.id == id }?.let {
            Result.Success(it)
        } ?: Result.Error("Could not find reminder")
    }

    override suspend fun deleteAllReminders() {
        remindersData = linkedMapOf()
    }

    override suspend fun delete(id: String) {
        val reminder = remindersData.values.flatten().firstOrNull { it.id == id }
        reminder?.let {
            remindersData[it.userId] = remindersData[it.userId]?.minus(it) ?: emptyList()
        }
    }
}