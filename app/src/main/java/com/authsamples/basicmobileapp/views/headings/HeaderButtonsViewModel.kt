package com.authsamples.basicmobileapp.views.headings

import androidx.databinding.BaseObservable

/*
 * A simple view model class for the header buttons fragment
 */
class HeaderButtonsViewModel : BaseObservable() {

    private var hasData = false

    /*
     * The binding system calls this and only enables buttons when we have data
     */
    fun hasData(): Boolean {
        return this.hasData
    }

    /*
     * The enabled state is updated during API calls and when logging out
     */
    fun updateDataStatus(hasData: Boolean) {
        this.hasData = hasData
        this.notifyChange()
    }
}
