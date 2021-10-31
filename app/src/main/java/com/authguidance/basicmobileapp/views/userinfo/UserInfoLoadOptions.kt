package com.authguidance.basicmobileapp.views.userinfo

/*
 * Special logic related to loading user info
 */
data class UserInfoLoadOptions(
    val reload: Boolean,
    val isInMainView: Boolean,
    val causeError: Boolean
)
