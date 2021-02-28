package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.not
import org.hamcrest.core.IsInstanceOf
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import org.hamcrest.Matchers.`is` as matches

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Use a fake repository to be injected into the viewmodel
    private lateinit var remindersDataSource: FakeDataSource

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        remindersDataSource = FakeDataSource()
        val reminder1 =
            ReminderDTO("Title1", "Description1", "location", 123.0, 456.0, "user1", "id1")
        val reminder2 =
            ReminderDTO("Title2", "Description2", "location", 123.0, 456.0, "user2", "id2")
        val reminder3 =
            ReminderDTO("Title3", "Description3", "location", 123.0, 456.0, "user1", "id3")
        remindersDataSource.addReminder(reminder1, reminder2, reminder3)
        val application = ApplicationProvider.getApplicationContext<Application>()

        saveReminderViewModel = SaveReminderViewModel(application, remindersDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun check_loading_saveReminder() {
        val newReminder =
            ReminderDataItem("Title4", "Description4", "location", 123.0, 456.0, "user1", "id3")

        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.saveReminder(newReminder)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), matches(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), matches(false))
        assertThat(saveReminderViewModel.reminderAdded.getOrAwaitValue(), matches(newReminder))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), matches("Reminder Saved !"))
    }

    @Test
    fun moveBackToRemindersList() {
        saveReminderViewModel.moveBackToReminderList()

        assertThat(saveReminderViewModel.reminderAdded.getOrAwaitValue(), IsNull())
        assertThat(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            IsInstanceOf(NavigationCommand.Back::class.java)
        )
    }

    @Test
    fun check_loading_removeExistingReminder() {
        val reminder1 =
            ReminderDataItem("Title1", "Description1", "location", 123.0, 456.0, "user1", "id1")
        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.removeReminder(reminder1)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), matches(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), matches(false))
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            matches(R.string.reminder_removed)
        )
    }

    @Test
    fun validateAndSaveReminder_ReminderMissingTitle() {
        val reminder =
            ReminderDataItem(
                title = null,
                description = "Description1",
                location = "location",
                latitude = 123.0,
                longitude = 456.0,
                userId = "user1",
                id = "id10"
            )

        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            matches(R.string.err_enter_title)
        )
        assertThat(
            remindersDataSource.remindersData.values.flatten().map { it.id },
            not(contains(reminder.id))
        )
    }

    @Test
    fun validateAndSaveReminder_ReminderMissingLocation() {
        val reminder =
            ReminderDataItem(
                title = "Title",
                description = "Description1",
                location = null,
                latitude = 123.0,
                longitude = 456.0,
                userId = "user1",
                id = "id11"
            )

        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            matches(R.string.err_select_location)
        )
        assertThat(
            remindersDataSource.remindersData.values.flatten().map { it.id },
            not(contains(reminder.id))
        )
    }

    @Test
    fun validateAndSaveReminder_SaveReminder() {
        val reminder =
            ReminderDataItem(
                title = "Title",
                description = "Description1",
                location = "location",
                latitude = 123.0,
                longitude = 456.0,
                userId = "user1",
                id = "id11"
            )

        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(saveReminderViewModel.reminderAdded.getOrAwaitValue(), matches(reminder))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), matches("Reminder Saved !"))
    }

    @Test
    fun updateSelectedLocation_LocationNeverSet() {
        saveReminderViewModel.updateSelectedLocation()

        assertThat(
            saveReminderViewModel.showErrorMessage.getOrAwaitValue(),
            matches("Please select a point of interest")
        )
    }

    @Test
    fun updateSelectedLocation_POILocationSet() {
        saveReminderViewModel.selectedPOI.postValue(
            PointOfInterest(
                LatLng(1.0, 2.0),
                "Id",
                "POI title"
            )
        )
        saveReminderViewModel.updateSelectedLocation()

        assertThat(
            saveReminderViewModel.latitude.getOrAwaitValue(),
            matches(1.0)
        )
        assertThat(
            saveReminderViewModel.longitude.getOrAwaitValue(),
            matches(2.0)
        )
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            matches("POI title")
        )
        assertThat(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            IsInstanceOf(NavigationCommand.Back::class.java)
        )
    }

    @Test
    fun onClear() {
        saveReminderViewModel.onClear()
        assertThat(
            saveReminderViewModel.longitude.getOrAwaitValue(),
            IsNull()
        )
        assertThat(
            saveReminderViewModel.latitude.getOrAwaitValue(),
            IsNull()
        )
        assertThat(
            saveReminderViewModel.reminderTitle.getOrAwaitValue(),
            IsNull()
        )
        assertThat(
            saveReminderViewModel.reminderDescription.getOrAwaitValue(),
            IsNull()
        )
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            IsNull()
        )
        assertThat(
            saveReminderViewModel.selectedPOI.getOrAwaitValue(),
            IsNull()
        )
    }
}