package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.FakeUserRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.collection.IsCollectionWithSize
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import org.hamcrest.Matchers.`is` as matches

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Use a fake repository to be injected into the viewmodel
    private lateinit var remindersDataSource: FakeDataSource
    private lateinit var userRepository: FakeUserRepository

    // Subject under test
    private lateinit var saveReminderViewModel: RemindersListViewModel

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        remindersDataSource = FakeDataSource()
        userRepository = FakeUserRepository()
        val application = ApplicationProvider.getApplicationContext<Application>()
        userRepository.setUserId("user1")

        saveReminderViewModel =
            RemindersListViewModel(application, remindersDataSource, userRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun showEmptyStateWhenRemindersListIsEmpty() {
        saveReminderViewModel.loadReminders()

        assertThat(saveReminderViewModel.showNoData.getOrAwaitValue(), matches(true))
    }

    @Test
    fun check_loading_RemindersLoading() {
        val reminder =
            ReminderDTO("Title3", "Description3", "location", 123.0, 456.0, "user1", "id3")
        remindersDataSource.addReminder(reminder)

        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.loadReminders()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), matches(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), matches(false))
        assertThat(
            saveReminderViewModel.remindersList.getOrAwaitValue(),
            IsCollectionWithSize(matches(1))
        )
    }

    @Test
    fun shouldReturnError() {
        saveReminderViewModel.loadReminders()

        assertThat(
            saveReminderViewModel.showSnackBar.getOrAwaitValue(),
            matches("Could not get reminders")
        )
    }
}