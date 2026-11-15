package com.example

import android.app.Activity
import android.os.Bundle

/**
 * Dummy Home activity used to reset the system default launcher preference.
 * Toggling this component state forces Android OS to re-prompt the user
 * with the launcher selection dialog ("Open with / Always") when pressing Home.
 */
class FakeHomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
