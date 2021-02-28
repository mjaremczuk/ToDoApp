package com.udacity.project4.locationreminders.data.local

import com.google.firebase.auth.FirebaseAuth

class FirebaseUserRepository : UserRepository {

    override fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    override fun isUserLoggedIn() = FirebaseAuth.getInstance().currentUser != null
}