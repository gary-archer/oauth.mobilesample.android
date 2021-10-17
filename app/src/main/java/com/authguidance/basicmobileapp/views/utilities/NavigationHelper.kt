package com.authguidance.basicmobileapp.views.utilities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.views.security.LoginRequiredFragment
import java.util.Locale
import java.util.regex.Pattern

/*
 * A helper class for dealing with navigation and deep linking, including the back stack
 */
class NavigationHelper(
    val navHostFragment: NavHostFragment,
    val isDeviceSecuredAccessor: () -> Boolean
) {
    lateinit var deepLinkBaseUrl: String

    /*
     * A utility method to navigate and manage the back stack
     */
    fun navigateTo(fragmentId: Int, args: Bundle? = null) {

        val activeFragment = this.navHostFragment.childFragmentManager.primaryNavigationFragment
        if (this.preNavigate(activeFragment, fragmentId)) {
            this.navHostFragment.navController.navigate(fragmentId, args)
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
                val activeFragment = this.navHostFragment.childFragmentManager.primaryNavigationFragment
                if (this.deepLinkDoNavigate(url, activeFragment)) {
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
    fun getDeepLinkUrl(receivedIntent: Intent?): String? {

        if (receivedIntent == null) {
            return null
        }

        if (receivedIntent.action.equals(Intent.ACTION_VIEW)) {
            val url = receivedIntent.dataString
            if (!url.isNullOrBlank()) {
                return url
            }
        }

        return null
    }

    /*
     * Some operations are disabled in this view
     */
    fun isInLoginRequired(): Boolean {

        val currentFragmentId = NavHostFragment.findNavController(this.navHostFragment).currentDestination?.id
        return currentFragmentId == R.id.login_required_fragment
    }

    /*
     * Navigate to a deep linking URL such as 'https://mobile.authsamples.com#company=2'
     * Our example is simplistic since we only have a couple of screens
     */
    private fun deepLinkDoNavigate(url: String, activeFragment: Fragment?): Boolean {

        var newFragmentId: Int? = null
        var args: Bundle? = null

        // Check for our deep linking URL
        val urlData = Uri.parse(url)
        val baseUrl = "${urlData.scheme}://${urlData.host}"
        if (baseUrl.toLowerCase(Locale.ROOT).equals(this.deepLinkBaseUrl) &&
            urlData.path?.toLowerCase(Locale.ROOT)?.startsWith("/basicmobileapp/deeplink", true)!!
        ) {

            // The default action is to move to the company list
            newFragmentId = R.id.companies_fragment

            // Check for a hash fragment
            val hash = urlData.fragment
            if (hash != null) {

                // If we have a company id then move to the transactions view
                val companyId = this.getDeepLinkedCompanyId(hash)
                if (companyId != null) {

                    newFragmentId = R.id.transactions_fragment
                    args = Bundle()
                    args.putString(Constants.ARG_COMPANY_ID, companyId)
                }
            }
        }

        // Navigate if required
        if (newFragmentId != null) {
            if (this.preNavigate(activeFragment, newFragmentId)) {
                this.navHostFragment.navController.navigate(newFragmentId, args)
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
    private fun preNavigate(activeFragment: Fragment?, newFragmentId: Int): Boolean {

        // When the device is not secured, only allow navigation to the Device Not Secured view
        if (!this.isDeviceSecuredAccessor() && newFragmentId != R.id.device_not_secured_fragment) {
            return false
        }

        // When navigating from the below pages, remove them from the back stack first
        // Note that the active fragment is null at application startup
        if (activeFragment == null || activeFragment is LoginRequiredFragment) {
            this.navHostFragment.navController.popBackStack()
        }

        return true
    }
}
