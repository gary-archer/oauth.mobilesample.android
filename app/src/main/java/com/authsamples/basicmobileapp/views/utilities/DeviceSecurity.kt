package com.authsamples.basicmobileapp.views.utilities

import android.app.KeyguardManager
import android.content.Context

/*
 * An object to ensure that the device is secured before running the app
 */
object DeviceSecurity {

    /*
     * See if the device has a PIN, Pattern or Fingerprint enabled
     */
    fun isDeviceSecured(context: Context): Boolean {

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }
}
