package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize
import org.hamcrest.collection.IsEmptyCollection
import org.hamcrest.core.IsInstanceOf
import org.hamcrest.core.IsNull.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.core.Is.`is` as matches

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    //    TODO: Add testing implementation to the RemindersLocalRepository.kt
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun saveReminder_GetReminder() = runBlocking {
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

        localRepository.saveReminder(reminderDto)

        val result = localRepository.getReminder(reminderDto.id)

        assertThat(result, notNullValue())
        result as Result.Success
        assertThat(result.data.title, matches(reminderDto.title))
        assertThat(result.data.description, matches(reminderDto.description))
        assertThat(result.data.location, matches(reminderDto.location))
        assertThat(result.data.latitude, matches(reminderDto.latitude))
        assertThat(result.data.longitude, matches(reminderDto.longitude))
        assertThat(result.data.userId, matches(reminderDto.userId))
        assertThat(result.data.id, matches(reminderDto.id))
    }

    @Test
    fun shouldShowError_GetReminder() = runBlocking {
        val result = localRepository.getReminder("random-id")

        assertThat(result, IsInstanceOf(Result.Error::class.java))
        result as Result.Error
        assertThat(result.message, matches("Reminder not found!"))
    }

    @Test
    fun saveReminder_DeleteAllReminders() = runBlocking {
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

        localRepository.saveReminder(reminderDto)

        val result = localRepository.getReminders("user1")

        assertThat(result, IsInstanceOf(Result.Success::class.java))
        result as Result.Success
        assertThat(result.data, IsCollectionWithSize(matches(1)))

        localRepository.deleteAllReminders()

        val newResult = localRepository.getReminders("user1")
        newResult as Result.Success
        assertThat(newResult.data, IsEmptyCollection())
    }

    @Test
    fun saveReminder_Delete() = runBlocking {
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

        localRepository.saveReminder(reminderDto)

        val result = localRepository.getReminders("user1")

        assertThat(result, IsInstanceOf(Result.Success::class.java))
        result as Result.Success
        assertThat(result.data, IsCollectionWithSize(matches(1)))

        localRepository.delete(reminderDto.id)

        val newResult = localRepository.getReminders("user1")
        newResult as Result.Success
        assertThat(newResult.data, IsEmptyCollection())
    }
}