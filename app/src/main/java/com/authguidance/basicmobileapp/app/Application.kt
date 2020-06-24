package com.authguidance.basicmobileapp.app

/*
 * Our custom application class allows multiple activities to communicate
 */
class Application : android.app.Application() {

    // Store a reference to the main activity
    private var mainActivity: MainActivity? = null

    /*
     * Store a reference to the main activity or null
     */
    fun setMainActivity(activity: MainActivity?) {
        this.mainActivity = activity
    }

    /*
     * Only deep link when the main activity is in a ready state
     */
    fun isMainActivityTopMost(): Boolean {

        if (this.mainActivity == null) {
            return true
        }

        return this.mainActivity?.isTopMost() ?: false
    }
}
