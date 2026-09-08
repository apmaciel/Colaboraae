package com.example.modelodeaprendizado

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Redirects to the login screen and finishes the current activity when no
 * Firebase user is signed in. Call right after super.onCreate() and before
 * setContentView(), returning early when this returns false.
 */
fun AppCompatActivity.requireLoggedInUser(): Boolean {
    if (FirebaseAuth.getInstance().currentUser == null) {
        val intent = Intent(this, Telalogin::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
        return false
    }
    return true
}
