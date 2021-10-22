package com.authguidance.basicmobileapp.plumbing.events

/*
 * An event to move between a loaded state when we have data and an unloaded state otherwise
 */
class DataStatusEvent(val loaded: Boolean) {

    /*
     * A dummy method to work around compiler warnings
     */
    fun used() {
    }
}
