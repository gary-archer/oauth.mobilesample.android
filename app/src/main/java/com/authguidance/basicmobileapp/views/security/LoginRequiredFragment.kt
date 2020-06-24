package com.authguidance.basicmobileapp.views.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentLoginRequiredBinding

/*
 * The fragment to indicate that a login is required
 */
class LoginRequiredFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentLoginRequiredBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = FragmentLoginRequiredBinding.inflate(inflater, container, false)
        return binding.root
    }
}
