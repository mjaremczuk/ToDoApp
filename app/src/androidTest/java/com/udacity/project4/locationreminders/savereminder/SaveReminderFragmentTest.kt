package com.udacity.project4.locationreminders.savereminder

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.BaseFragmentTest
import com.udacity.project4.R
import com.udacity.project4.ToastMatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class SaveReminderFragmentTest : BaseFragmentTest() {

    @Test
    fun showSnackbarError_selectLocation() = runBlockingTest {
        launchFragmentInContainer<SaveReminderFragment>(null, R.style.AppTheme)

        onView(withId(R.id.reminderTitle)).perform(typeText("Reminder title"), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(
            typeText("Reminder description"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("Please select location")).check(matches(isDisplayed()))
    }

    @Test
    fun showSnackbarError_setTitle() = runBlockingTest {
        saveReminderViewModel.longitude.postValue(123.0)
        saveReminderViewModel.latitude.postValue(345.0)
        saveReminderViewModel.reminderSelectedLocationStr.postValue("Selected reminder")

        launchFragmentInContainer<SaveReminderFragment>(null, R.style.AppTheme)

        onView(withId(R.id.reminderDescription)).perform(
            typeText("Reminder description"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("Please enter title")).check(matches(isDisplayed()))
    }

    @Test
    fun showToast_NavigateBack_ReminderSaved() = runBlockingTest {
        saveReminderViewModel.longitude.postValue(123.0)
        saveReminderViewModel.latitude.postValue(45.0)
        saveReminderViewModel.reminderSelectedLocationStr.postValue("Selected reminder")

        val scenario = launchFragmentInContainer<SaveReminderFragment>(null, R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.reminderTitle)).perform(typeText("Reminder title"), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(
            typeText("Reminder description"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.saveReminder)).perform(click())

        verify(navController).popBackStack()
        onView(withText("Reminder Saved !")).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }

    @Test
    fun navigate_SelectLocationFragment() = runBlockingTest {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(null, R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.selectLocation)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.selectLocation)).perform(click())

        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }
}