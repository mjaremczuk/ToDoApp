package com.udacity.project4.locationreminders.reminderslist

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest
import androidx.test.filters.MediumTest
import com.udacity.project4.BaseFragmentTest
import com.udacity.project4.R
import com.udacity.project4.RecyclerViewItemCountAssertion
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : BaseFragmentTest() {

    @Test
    fun remindersList_DisplayedUI() = runBlockingTest {
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
        repository.saveReminder(reminderDto)

        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(RecyclerViewItemCountAssertion.withItemCount(1))

        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderCardView)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("title")))
        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(withText("description")))
        onView(withId(R.id.location)).check(matches(isDisplayed()))
        onView(withId(R.id.location)).check(matches(withText("LocationName")))
    }

    @Test
    fun navigateToSignInScreen() = runBlockingTest {
        userRepository.setUserId(null)
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.actionReminderListFragmentToLoginNavigation()
        )
    }

    @Test
    fun navigateToSaveReminder() = runBlockingTest {
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun refreshReminderList() = runBlockingTest {
        userRepository.setUserId("user1")
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderssRecyclerView))
            .check(RecyclerViewItemCountAssertion.withItemCount(0))

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
        repository.saveReminder(reminderDto)

        onView(withId(R.id.refreshLayout)).perform(swipeDown())

        onView(withId(R.id.reminderssRecyclerView))
            .check(RecyclerViewItemCountAssertion.withItemCount(1))
    }

    @FlakyTest(detail = "Sometimes test fails due to not matched final text")
    @Test
    fun showSnackBarError_FailedLoadReminders() = runBlockingTest {
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withText("Could not get reminders")).check(matches(isDisplayed()))

//        onView(withId(R.id.refreshLayout)).perform(swipeDown())

//        onView(withId(R.id.reminderssRecyclerView))
//            .check(RecyclerViewItemCountAssertion.withItemCount(0))
//        onView(withText("Could not get reminders")).check(matches(isDisplayed()))
    }
}