package com.authguidance.basicmobileapp.app

/*
 * View model actions that invoke events on the main activity
 */
interface MainActivityEvents {

    /*
     * Notify the view when the user is prompted to sign in
     */
    fun onLoginRequired()

    /*
     * Notify the view when the load state changes
     */
    fun onMainLoadStateChanged(loaded: Boolean)
}
