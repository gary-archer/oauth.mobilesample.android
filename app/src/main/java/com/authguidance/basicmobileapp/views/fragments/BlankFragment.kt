package com.authguidance.basicmobileapp.views.fragments

import android.content.Context

/*
 * An empty fragment rendered as the default
 */
class BlankFragment : androidx.fragment.app.Fragment() {

    /*
     * Get properties from the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        println("GJA: IN BLANK VIEW")
    }
}