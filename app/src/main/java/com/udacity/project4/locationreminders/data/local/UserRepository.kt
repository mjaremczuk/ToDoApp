package com.udacity.project4.locationreminders.data.local

interface UserRepository {

    fun getCurrentUserId(): String?

    fun isUserLoggedIn(): Boolean
}