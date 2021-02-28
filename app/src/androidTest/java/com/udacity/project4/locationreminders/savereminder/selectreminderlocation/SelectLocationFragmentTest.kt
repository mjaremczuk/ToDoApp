package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.location.Location
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
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
class SelectLocationFragmentTest : BaseFragmentTest() {

    @Test
    fun selectPointOfInterest() = runBlockingTest {
        val scenario = launchFragmentInContainer<SelectLocationFragment>(null, R.style.AppTheme)

        scenario.onFragment {
            it.fusedLocationClient.setMockMode(true)
            it.fusedLocationClient.setMockLocation(Location("Tests").apply {
                latitude = 37.4259
                longitude = -122.0723
                accuracy = 16F
            })
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.map)).perform(click())
        onView(ViewMatchers.withId(R.id.save_marker)).perform(click())

        onView(ViewMatchers.withText("Please select a point of interest"))
            .inRoot(ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.map)).perform(click())
        onView(ViewMatchers.withId(R.id.save_marker)).perform(click())

        verify(navController).popBackStack()
    }
}