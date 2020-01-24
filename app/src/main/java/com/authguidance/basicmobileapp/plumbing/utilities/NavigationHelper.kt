package com.authguidance.basicmobileapp.plumbing.utilities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.views.fragments.LoginRequiredFragment
import java.util.Locale
import java.util.regex.Pattern

/*
 * A helper class for dealing with navigation and deep linking, including the back stack
 */
class NavigationHelper {

    /*
     * A utility method to navigate and manage the back stack
     */
    fun navigate(navController: NavController, activeFragment: Fragment?, fragmentId: Int, args: Bundle? = null) {
        this.preNavigate(navController, activeFragment)
        navController.navigate(fragmentId, args)
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
    fun navigateToDeepLink(intent: Intent?, navController: NavController, activeFragment: Fragment?): Boolean {

        if (intent != null) {
            val url = this.getDeepLinkUrl(intent)
            if (url != null) {
                if (this.doNavigate(url, navController, activeFragment)) {
                    return true
                }
            }
        }

        return false
    }

    /*
     * Try to return a deep link URL from an intent
     */
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
     * Navigate to a deep linking URL such as 'https://authguidance-examples.com#company=2'
     * Our example is simplistic since we only have a couple of screens
     */
    private fun doNavigate(url: String, navController: NavController, activeFragment: Fragment?): Boolean {

        var id: Int? = null
        var args: Bundle? = null

        // Check for our deep linking URL
        val urlData = Uri.parse(url)
        if (urlData.host?.toLowerCase(Locale.ROOT).equals("authguidance-examples.com")) {

            // The default action is to move to the company list
            id = R.id.companiesFragment

            // Check for a hash fragment
            val hash = urlData.fragment
            if (hash != null) {

                // If we have a company id then move to the transactions view
                val companyId = this.getDeepLinkedCompanyId(hash)
                if (companyId != null) {

                    id = R.id.transactionsFragment
                    args = Bundle()
                    args.putString(Constants.ARG_COMPANY_ID, companyId)
                }
            }
        }

        // Navigate if required
        if (id != null) {
            this.preNavigate(navController, activeFragment)
            navController.navigate(id, args)
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
     * When navigating from the below pages, remove them from the back stack first
     * Note that the active fragment is null at application startup, when the blank fragment has not been rendered yet
     */
    private fun preNavigate(navController: NavController, activeFragment: Fragment?) {

        if (activeFragment == null || activeFragment is LoginRequiredFragment) {
            navController.popBackStack()
        }
    }
}