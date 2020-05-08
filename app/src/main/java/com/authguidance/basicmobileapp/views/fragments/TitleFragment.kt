package com.authguidance.basicmobileapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentTitleBinding

/*
 * The title fragment shows the logged in user
 */
class TitleFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentTitleBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = FragmentTitleBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * When logged in, call the API to get user info for display
     */
    fun loadUserInfo() {
        val userInfoFragment = this.childFragmentManager.findFragmentById(R.id.userInfoFragment) as UserInfoFragment
        userInfoFragment.loadUserInfo()
    }

    /*
     * Clear user info after logging out
     */
    fun clearUserInfo() {
        val userInfoFragment = this.childFragmentManager.findFragmentById(R.id.userInfoFragment) as UserInfoFragment
        userInfoFragment.clearUserInfo()
    }
}
