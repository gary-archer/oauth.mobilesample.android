package authguidance.mobilesample.plumbing.utilities

import android.util.Log

/*
 * A simple logger helper class
 */
class MobileLogger {

    /*
     * Output some debug info with a prefix that we can filter on in the logcat tool
     */
    companion object {
        @JvmStatic
        fun debug(info: String?) {
            Log.d("BasicMobileApp", info)
        }
    }
}