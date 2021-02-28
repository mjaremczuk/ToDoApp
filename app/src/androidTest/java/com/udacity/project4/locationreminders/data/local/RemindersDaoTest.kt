package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize
import org.hamcrest.collection.IsEmptyCollection
import org.hamcrest.core.IsNull.notNullValue
import org.hamcrest.core.IsNull.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.`is` as matches

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

    }

    @After
    fun tearDown() = database.close()

    @Test
    fun insertReminder() = runBlockingTest {
        val reminderDto =
            ReminderDTO(
                "title",
                "description",
                "LocationName",
                123.0,
                456.0,
                "user1",
                "id1"
            )

        database.reminderDao().saveReminder(reminderDto)

        val reminder = database.reminderDao().getReminderById(reminderDto.id)

        assertThat<ReminderDTO>(reminder as ReminderDTO, notNullValue())
        assertThat(reminder.id, matches(reminder.id))
        assertThat(reminder.title, matches(reminder.title))
        assertThat(reminder.description, matches(reminder.description))
        assertThat(reminder.location, matches(reminder.location))
        assertThat(reminder.latitude, matches(reminder.latitude))
        assertThat(reminder.longitude, matches(reminder.longitude))
        assertThat(reminder.userId, matches(reminder.userId))
        assertThat(reminder.id, matches(reminder.id))
    }

    @Test
    fun getListOfReminders() = runBlockingTest {
        val reminders = database.reminderDao().getReminders("user1")

        assertThat(reminders, IsEmptyCollection())

        val reminderDto =
            ReminderDTO(
                "title",
                "description",
                "LocationName",
                123.0,
                456.0,
                "user1",
                "id1"
            )

        database.reminderDao().saveReminder(reminderDto)
        database.reminderDao().saveReminder(reminderDto.copy(id = "id2"))

        val newReminders = database.reminderDao().getReminders("user1")

        assertThat(newReminders, IsCollectionWithSize(matches(2)))
    }

    @Test
    fun updateReminder() = runBlockingTest {
        val reminderDto =
            ReminderDTO(
                "title",
                "description",
                "LocationName",
                123.0,
                456.0,
                "user1",
                "id1"
            )

        database.reminderDao().saveReminder(reminderDto)

        val reminder = database.reminderDao().getReminderById(reminderDto.id)

        assertThat<ReminderDTO>(reminder as ReminderDTO, notNullValue())
        assertThat(reminder.id, matches(reminder.id))
        assertThat(reminder.title, matches(reminder.title))
        assertThat(reminder.description, matches(reminder.description))
        assertThat(reminder.location, matches(reminder.location))
        assertThat(reminder.latitude, matches(reminder.latitude))
        assertThat(reminder.longitude, matches(reminder.longitude))
        assertThat(reminder.userId, matches(reminder.userId))
        assertThat(reminder.id, matches(reminder.id))

        database.reminderDao().saveReminder(reminderDto.copy(title = "Updated title"))

        val updatedReminder = database.reminderDao().getReminderById(reminderDto.id)
        assertThat<ReminderDTO>(updatedReminder as ReminderDTO, notNullValue())
        assertThat(updatedReminder.id, matches(reminder.id))
        assertThat(updatedReminder.title, matches("Updated title"))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        val reminderDto =
            ReminderDTO(
                "title",
                "description",
                "LocationName",
                123.0,
                456.0,
                "user1",
                "id1"
            )
        database.reminderDao().saveReminder(reminderDto)
        database.reminderDao().saveReminder(reminderDto.copy(id = "id2"))

        val reminders = database.reminderDao().getReminders("user1")

        assertThat(reminders, IsCollectionWithSize(matches(2)))

        database.reminderDao().deleteAllReminders()

        val newReminders = database.reminderDao().getReminders("user1")

        assertThat(newReminders, IsEmptyCollection())
    }

    @Test
    fun deleteReminderById() = runBlockingTest {
        val reminderDto =
            ReminderDTO(
                "title",
                "description",
                "LocationName",
                123.0,
                456.0,
                "user1",
                "id1"
            )

        database.reminderDao().saveReminder(reminderDto)

        val reminder = database.reminderDao().getReminderById(reminderDto.id)

        assertThat<ReminderDTO>(reminder as ReminderDTO, notNullValue())

        database.reminderDao().delete(reminderDto.id)

        val newReminder = database.reminderDao().getReminderById(reminderDto.id)

        assertThat<ReminderDTO>(newReminder, nullValue())

    }
}