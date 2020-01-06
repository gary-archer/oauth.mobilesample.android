package com.authguidance.basicmobileapp.plumbing.utilities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.views.fragments.LoginRequiredFragment
import java.util.regex.Matcher
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
    fun navigateToDeepLink(intent: Intent?, navController: NavController, activeFragment: Fragment?) : Boolean {

        if(intent != null) {
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

        if(receivedIntent == null) {
            return null
        }

        if (receivedIntent.action.equals(Intent.ACTION_VIEW)) {
            val url = receivedIntent.dataString
            if (!url.isNullOrBlank()) {
                return url;
            }
        }

        return null
    }

    /*
     * Navigate to a deep linking URL and return true on success
     */
    private fun doNavigate(url: String, navController: NavController, activeFragment: Fragment?): Boolean {

        var id: Int? = null
        var args: Bundle? = null

        // First try to match the transactions URL
        var matcher = this.matchUrl("^https://authguidance-examples.com/companies/([^/]+)/transactions$", url)
        if(matcher.find() && matcher.groupCount() >= 1) {
            id = R.id.transactionsFragment
            val companyId = matcher.group(1)
            args = Bundle()
            args.putString(Constants.ARG_COMPANY_ID, companyId)
        }

        // Next try to match the companies URL
        matcher = this.matchUrl("^https://authguidance-examples.com/companies$", url)
        if(matcher.find()) {
            id = R.id.companiesFragment
        }

        // Navigate if required
        if(id != null) {
            this.preNavigate(navController, activeFragment)
            navController.navigate(id, args)
            return true
        }

        // Otherwise indicate that the deep link was not found
        return false
    }

    /*
     * Match incoming URLs to a RegEx pattern
     */
    private fun matchUrl(patternToMatch: String, url: String): Matcher {
        val pattern = Pattern.compile(patternToMatch)
        return pattern.matcher(url)
    }

    /*
     * When navigating from these pages, remove them from the back stack first
     * Note that the active fragment is null at application startup, when the blank fragment has not been rendered yet
     */
    private fun preNavigate(navController: NavController, activeFragment: Fragment?) {

        if( activeFragment == null ||
            activeFragment is LoginRequiredFragment)
        {
            navController.popBackStack()
        }
    }
}