package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentUserInfoBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.views.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * The user info fragment shows the logged in user
 */
class UserInfoFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentUserInfoBinding
    private lateinit var mainActivity: MainActivity

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * When logged in, call the API to get user info for display
     */
    fun loadUserInfo() {

        // First clear any previous errors
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.userInfoErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()

        // Try to get data
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@UserInfoFragment
            try {

                // Load user info
                val userInfo = that.mainActivity.getApiClient().getUserInfo()

                withContext(Dispatchers.Main) {

                    // Render user info
                    that.mainActivity.viewManager.onUserInfoLoaded()
                    that.binding.loggedInUser.text = "${userInfo.givenName} ${userInfo.familyName}"
                }
            }
            catch(uiError: UIError) {

                withContext(Dispatchers.Main) {

                    // Report errors calling the API
                    that.mainActivity.viewManager.onUserInfoLoadFailed(uiError)

                    // Render error details
                    errorFragment.reportError(
                        that.getString(R.string.userinfo_error_hyperlink),
                        that.getString(R.string.userinfo_error_dialogtitle),
                        uiError)
                }
            }
        }
    }

    /*
     * Clear data after logging out
     */
    fun clearUserInfo() {

        // Blank out the user name
        this.binding.loggedInUser.text = ""

        // Also ensure that any errors are cleared
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.userInfoErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()
    }
}
