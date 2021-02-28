package com.udacity.project4

import com.udacity.project4.locationreminders.data.local.UserRepository

class FakeUserRepository : UserRepository {

    var currentId: String? = null

    fun setUserId(id: String?) {
        currentId = id
    }

    override fun getCurrentUserId() = currentId

    override fun isUserLoggedIn() = currentId != null
}