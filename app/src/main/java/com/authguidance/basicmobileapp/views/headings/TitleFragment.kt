package com.authguidance.basicmobileapp.views.headings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
}
