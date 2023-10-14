package com.authsamples.basicmobileapp.views.utilities

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavHostController
import java.util.Locale
import java.util.regex.Pattern

/*
 * A view helper class for dealing with navigation and deep linking, including the back stack
 */
class NavigationHelper(
    private val navHostController: NavHostController,
    private val isDeviceSecured: () -> Boolean
) {
    lateinit var deepLinkBaseUrl: String

    /*
     * Return the current view
     */
    fun getActiveViewName(): String? {
        return this.navHostController.currentDestination?.route
    }

    /*
     * A utility method to navigate and manage the back stack
     */
    fun navigateTo(viewName: String) {

        if (this.preNavigate(this.getActiveViewName(), viewName)) {
            this.navHostController.navigate(viewName)
        }
    }

    /*
     * See if this is a deep link
     */
    fun isDeepLinkIntent(intent: Intent?): Boolean {
        return this.getDeepLinkUrl(intent) != null
    }

    /*
     * Given a deep linking intent, navigate to it and return true on success
     */
    fun navigateToDeepLink(intent: Intent?): Boolean {

        if (intent != null) {
            val url = this.getDeepLinkUrl(intent)
            if (url != null) {
                val activeView = getActiveViewName()
                if (this.deepLinkDoNavigate(url, activeView)) {
                    return true
                }
            }
        }

        return false
    }

    /*
     * Try to return a deep link URL from an intent
     */
    @Suppress("ReturnCount")
    private fun getDeepLinkUrl(receivedIntent: Intent?): String? {

        if (receivedIntent == null) {
            return null
        }

        if (receivedIntent.action == Intent.ACTION_VIEW) {
            val url = receivedIntent.dataString
            if (!url.isNullOrBlank()) {
                return url
            }
        }

        return null
    }

    /*
     * Navigate to a deep linking URL such as 'https://mobile.authsamples.com#company=2'
     * Our example is simplistic since we only have a couple of screens
     */
    private fun deepLinkDoNavigate(url: String, activeViewName: String?): Boolean {

        var newViewName: String? = null

        // Check for our deep linking URL
        val urlData = Uri.parse(url)
        val baseUrl = "${urlData.scheme}://${urlData.host}"
        if (baseUrl.lowercase(Locale.ROOT) == this.deepLinkBaseUrl &&
            urlData.path?.lowercase(Locale.ROOT)?.startsWith("/basicmobileapp/deeplink", true)!!
        ) {

            // The default action is to move to the company list
            newViewName = MainView.Companies

            // Check for a hash fragment
            val hash = urlData.fragment
            if (hash != null) {

                // If we have a company id then move to the transactions view
                val companyId = this.getDeepLinkedCompanyId(hash)
                if (companyId != null) {
                    newViewName = "${MainView.Transactions}/$companyId"
                }
            }
        }

        // Navigate if required
        if (newViewName != null) {
            if (this.preNavigate(activeViewName, newViewName)) {
                this.navHostController.navigate(newViewName)
            }
            return true
        }

        // Otherwise indicate that the deep link was not found
        return false
    }

    /*
     * See if the hash fragment is of the form '#company=2' and if so return the id
     */
    private fun getDeepLinkedCompanyId(hashFragment: String): String? {

        val pattern = Pattern.compile("^company=(.+)")
        val matcher = pattern.matcher(hashFragment)
        if (matcher.find() && matcher.groupCount() >= 1) {
            return matcher.group(1)
        }

        return null
    }

    /*
     * Return true to allow navigation to proceed
     */
    private fun preNavigate(activeViewName: String?, newViewName: String): Boolean {

        // When the device is not secured, only allow navigation to the Device Not Secured view
        if (!this.isDeviceSecured() && newViewName != MainView.DeviceNotSecured) {
            return false
        }

        // When navigating from the below pages, remove them from the back stack first
        // Note that the active view is null at application startup
        if (activeViewName == null || activeViewName == MainView.LoginRequired) {
            this.navHostController.popBackStack()
        }

        return true
    }
}
