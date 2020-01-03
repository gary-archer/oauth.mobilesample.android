package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentActionBarBinding
import com.authguidance.basicmobileapp.views.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * The action bar fragment shows the logged in user
 */
class ActionBarFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentActionBarBinding
    private lateinit var mainActivity: MainActivity

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentActionBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * When logged in, call the API to get user info for display
     */
    fun loadUserInfo() {

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@ActionBarFragment
            try {

                // Load user info
                val userInfo = that.mainActivity.getApiClient().getUserInfo()

                withContext(Dispatchers.Main) {

                    // Render user info
                    that.binding.loggedInUser.text = "${userInfo.givenName} ${userInfo.familyName}"
                }
            }
            catch(ex: Exception) {

                // Report errors such as those looking up endpoints
                withContext(Dispatchers.Main) {
                    that.mainActivity.handleException(ex)
                }
            }
        }
    }

    /*
     * Clear user info after logging out
     */
    fun clearUserInfo() {
        this.binding.loggedInUser.text = ""
    }
}
