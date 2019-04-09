package authguidance.mobilesample.plumbing.utilities

/*
 * A simple logger class
 */
class MobileLogger {

    // TODO: Use Log.d(TAG, "whatevar");

    /*
     * Output some debug info with a prefix that we can filter on in the logcat tool
     */
    fun debug(info: String) {
        println("BasicMobileApp: $info")
    }
}