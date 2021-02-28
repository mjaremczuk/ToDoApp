package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FirebaseUserRepository
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.data.local.UserRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Rule @JvmField
    val mRuntimePermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource,
                    get() as UserRepository
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { FirebaseUserRepository() as UserRepository }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        //clear the data to start fresh
        runBlocking {
            FirebaseAuth.getInstance().signOut()
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun signIn_AddReminderAndSignOut() = runBlocking {
        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        enterEmailAuthorization("michaljaremczuk@gmail.com")

        onView(withId(R.id.password)).perform(typeText("12345a"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.button_done)).perform(click())
        onView(withText("Location Reminders")).check(matches(isCompletelyDisplayed()))

        createReminder()

        onView(withId(R.id.reminderssRecyclerView))
            .check(RecyclerViewItemCountAssertion.withItemCount(1))
        onView(withId(R.id.logout)).perform(click())
        onView(withId(R.id.log_in)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun register_CreateReminder() = runBlocking {
        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        enterEmailAuthorization("${System.currentTimeMillis()}-test@gmail.com")

        onView(withId(R.id.name)).perform(typeText("John Doe"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.password)).perform(typeText("12345a"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.button_create)).perform(click())

        onView(withText("Location Reminders")).check(matches(isCompletelyDisplayed()))
        createReminder()

        onView(withId(R.id.reminderssRecyclerView))
            .check(RecyclerViewItemCountAssertion.withItemCount(1))
        onView(withId(R.id.logout)).perform(click())

        activityScenario.close()
    }

    private fun enterEmailAuthorization(email: String) {
        onView(withId(R.id.log_in)).check(matches(isDisplayed()))
        onView(withId(R.id.log_in)).perform(click())
        onView(withId(R.id.email_button)).perform(click())
        onView(withId(R.id.email)).perform(
            typeText(email),
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.button_next)).perform(click())
    }

    private fun createReminder() {
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle))
            .perform(typeText("Reminder title"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(
            typeText("Reminder description"),
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(click())

        onView(withId(R.id.save_marker)).perform(click())
        onView(ViewMatchers.withText("Please select a point of interest"))
            .inRoot(ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.map)).perform(click())
        onView(ViewMatchers.withId(R.id.save_marker)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())
    }
}
