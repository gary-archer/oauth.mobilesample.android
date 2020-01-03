package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentLoginRequiredBinding
import com.authguidance.basicmobileapp.views.activities.MainActivity

/*
 * The fragment to indicate that a login is required
 */
class LoginRequiredFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentLoginRequiredBinding
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

        this.binding = FragmentLoginRequiredBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Update state after creation
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.mainActivity.setButtonState()
        this.mainActivity.setFragmentTitle(this.getString(R.string.login_required_title))
    }
}
