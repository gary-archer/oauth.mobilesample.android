package com.authsamples.finalmobileapp.views.utilities

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
    private var currentViewPath: String = MainView.Companies

    /*
     * Return the current view
     */
    fun getActiveViewName(): String? {
        return this.navHostController.currentDestination?.route
    }

    /*
     * Move to the device not secured view
     */
    fun navigateToDeviceNotSecured() {

        if (this.preNavigate(MainView.DeviceNotSecured)) {
            this.navHostController.navigate(MainView.DeviceNotSecured)
        }
    }

    /*
     * Move to the login required view while maintaining the current view path to move to afterwards
     */
    fun navigateToLoginRequired() {

        if (this.preNavigate(MainView.LoginRequired)) {
            this.navHostController.navigate(MainView.LoginRequired)
        }
    }

    /*
     * After an explicit logout, move to the login required view and reset the current view path
     */
    fun navigateToLoggedOut() {

        this.currentViewPath = MainView.Companies
        this.navigateToLoginRequired()
    }

    /*
     * Navigate to an application path
     */
    fun navigateToPath(viewPath: String) {

        if (this.preNavigate(viewPath)) {
            currentViewPath = viewPath
            this.navHostController.navigate(viewPath)
        }
    }

    /*
     * Navigation after login
     */
    fun navigateAfterLogin() {
        this.navHostController.navigate(currentViewPath)
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
                if (this.deepLinkDoNavigate(url)) {
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
     * Navigate to a deep linking URL such as 'https://mobile.authsamples.com/finalmobileapp/deeplink/companies/2'
     */
    private fun deepLinkDoNavigate(url: String): Boolean {

        val newViewPath = calculateDeepLinkViewPath(url)
        if (newViewPath != null) {
            this.navigateToPath(newViewPath)
            return true
        }

        return false
    }

    /*
     * Get a view path from a deep link path
     */
    private fun calculateDeepLinkViewPath(url: String): String? {

        var newViewPath: String? = null

        // Check for our deep linking URL
        val urlData = Uri.parse(url)
        val baseUrl = "${urlData.scheme}://${urlData.host}"
        val deepLinkBasePath = "/finalmobileapp/deeplink"
        val lowerCasePath = urlData.path?.lowercase(Locale.ROOT)
        if (baseUrl.lowercase(Locale.ROOT) == this.deepLinkBaseUrl &&
            lowerCasePath?.startsWith(deepLinkBasePath, true)!!
        ) {

            // The default action is to move to the company list
            newViewPath = MainView.Companies

            // If we have a transactions view path of the form /companies/2 then move to the transactions view
            val relativePath = lowerCasePath.replace("$deepLinkBasePath/", "")
            val companyId = this.getDeepLinkedCompanyId(relativePath)
            if (companyId != null) {
                newViewPath = "${MainView.Transactions}/$companyId"
            }
        }

        return newViewPath
    }

    /*
     * See if the relative path is of the form 'companies/2' and if so return the id
     */
    private fun getDeepLinkedCompanyId(relativePath: String): String? {

        val pattern = Pattern.compile("^companies/(.+)")
        val matcher = pattern.matcher(relativePath)
        if (matcher.find() && matcher.groupCount() >= 1) {
            return matcher.group(1)
        }

        return null
    }

    /*
     * Return true to allow navigation to proceed
     */
    private fun preNavigate(newViewName: String): Boolean {

        // Do not allow navigating back to the initial blank view
        val activeViewName = this.getActiveViewName()
        if (activeViewName == MainView.Blank) {
            this.navHostController.popBackStack()
        }

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
