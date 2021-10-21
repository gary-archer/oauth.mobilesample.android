package com.authguidance.basicmobileapp.app

/*
 * View model actions that invoke events on the main activity
 */
interface MainActivityEvents {

    fun onHome()

    fun onReloadData(causeError: Boolean)

    fun onStartLogout()
}
