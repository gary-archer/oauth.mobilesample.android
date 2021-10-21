package com.authguidance.basicmobileapp.plumbing.events

/*
 * An event to move between a loaded state when we have data and an unloaded state otherwise
 */
class GetDataEvent(val loaded: Boolean) {

    /*
     * A dummy method to work around compiler warnings
     */
    fun used() {
    }
}
