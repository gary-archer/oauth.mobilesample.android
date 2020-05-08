package com.authguidance.basicmobileapp.plumbing.utilities

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import com.authguidance.basicmobileapp.R

/*
 * A class to ensure that the device is secured before running the app
 */
class DeviceSecurity() {

    companion object {

        /*
         * See if the device has a PIN, Pattern or Fingerprint enabled
         */
        fun isDeviceSecured(context: Context): Boolean {

            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return keyguardManager.isDeviceSecure
            } else {
                return keyguardManager.isKeyguardSecure
            }
        }

            /*
         * Use the technique from the below article to force a lock screen to be set
         * https://proandroiddev.com/secure-data-in-android-encryption-in-android-part-2-991a89e55a23
         */
        fun forceLockScreenUpdate(context: Context, agreeCallback: () -> Unit, declineCallback: () -> Unit) {

            AlertDialog.Builder(context)
                .setTitle(R.string.lock_title)
                .setMessage(R.string.lock_body)
                .setPositiveButton(R.string.lock_settings) { _, _ -> agreeCallback() }
                .setNegativeButton(R.string.lock_exit) { _, _ -> declineCallback() }
                .setCancelable(false)
                .show()
        }
    }
}