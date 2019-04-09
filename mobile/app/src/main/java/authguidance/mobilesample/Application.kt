package authguidance.mobilesample

import authguidance.mobilesample.plumbing.utilities.MobileLogger

/*
 * The application class
 */
class Application : android.app.Application() {

    /*
     * Override application startup
     */
    override fun onCreate() {
        super.onCreate()

        var logger = MobileLogger();
        logger.debug("Application Startup");
    }
}