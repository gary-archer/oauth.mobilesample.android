package com.authsamples.basicmobileapp.views.utilities

/*
 * Options supplied from views when they trigger a load
 */
data class ViewLoadOptions(
    val forceReload: Boolean,
    val causeError: Boolean
)
