package com.authguidance.basicmobileapp.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.plumbing.errors.ErrorField
import kotlinx.android.synthetic.main.error_list_item.view.*

/*
 * An adapter to render transaction items
 */
class ErrorItemArrayAdapter(val context: Context, val errorItems: List<ErrorField>) : RecyclerView.Adapter<ErrorItemArrayAdapter.ViewHolder>() {

    /*
     * Inflate this list item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(this.context)
        return ViewHolder(inflater.inflate(R.layout.error_list_item, parent, false))
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.errorItems.size
    }

    /*
     * Binds an item to a view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentField = this.errorItems[position]
        holder.item.errorField.text = currentField.name
        holder.item.errorValue.text = currentField.value
    }

    /*
     * Stores and recycles views as they are scrolled off screen
     */
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val item = itemView
    }
}