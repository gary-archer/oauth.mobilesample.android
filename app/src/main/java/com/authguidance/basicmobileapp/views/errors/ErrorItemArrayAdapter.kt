package com.authguidance.basicmobileapp.views.errors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.authguidance.basicmobileapp.databinding.ErrorListItemBinding

/*
 * An adapter to render error items
 */
class ErrorItemArrayAdapter(
    val context: Context,
    val errorLines: List<ErrorListItemViewModel>
) : RecyclerView.Adapter<ErrorListItemViewHolder>() {

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

        val currentErrorLine = this.errorLines[position]
        holder.bind(currentErrorLine)
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.errorLines.size
    }
}
