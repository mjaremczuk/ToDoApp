package com.udacity.project4

import android.app.Application
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.UserRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito

abstract class BaseFragmentTest: AutoCloseKoinTest() {

    protected lateinit var repository: ReminderDataSource
    protected lateinit var userRepository: FakeUserRepository
    protected lateinit var appContext: Application
    protected lateinit var saveReminderViewModel: SaveReminderViewModel

    protected val navController = Mockito.mock(NavController::class.java)

    @Rule @JvmField
    val mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Before
    fun init() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
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
            single { FakeUserRepository() as UserRepository }
            single { FakeDataSource() as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        repository = get()
        val userRepository: UserRepository = get()
        this.userRepository = userRepository as FakeUserRepository
        saveReminderViewModel = get()

        runBlocking {
            userRepository.setUserId("user1")
            repository.deleteAllReminders()
        }
    }
}