package com.authsamples.basicmobileapp.views.errors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.authsamples.basicmobileapp.databinding.ErrorListItemBinding
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFormatter
import com.authsamples.basicmobileapp.plumbing.errors.ErrorLine

/*
 * An adapter to render error items in a recycler view
 */
class ErrorItemArrayAdapter(
    val context: Context,
    val model: ErrorDetailsViewModel
) : RecyclerView.Adapter<ErrorListItemViewHolder>() {

    private val errorLines: List<ErrorLine>

    init {
        this.errorLines = ErrorFormatter(context).getErrorLines(this.model.error)
    }

    /*
     * Inflate this list item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorListItemViewHolder {

        val inflater = LayoutInflater.from(this.context)
        val itemBinding = ErrorListItemBinding.inflate(inflater, parent, false)
        return ErrorListItemViewHolder(itemBinding)
    }

    /*
     * Binds an item to a view
     */
    override fun onBindViewHolder(holder: ErrorListItemViewHolder, position: Int) {

        val viewModelItems = this.errorLines.map {
            ErrorListItemViewModel(it)
        }

        val currentErrorLine = viewModelItems[position]
        holder.bind(currentErrorLine)
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.errorLines.size
    }
}
