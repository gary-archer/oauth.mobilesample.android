package com.authguidance.basicmobileapp.views.companies

import androidx.recyclerview.widget.RecyclerView
import com.authguidance.basicmobileapp.databinding.CompanyListItemBinding

/*
 * A custom view holder class for company rows in a recycler view
 */
class CompanyItemViewHolder(
    val layoutBinding: CompanyListItemBinding
) : RecyclerView.ViewHolder(layoutBinding.root) {

    /*
     * Bind a data item to a layout row
     */
    fun bind(item: CompanyItemViewModel) {
        this.layoutBinding.model = item
        this.layoutBinding.executePendingBindings()
    }

    /*
     * Handle click events
     */
    fun onClick(company: CompanyItemViewModel, listener: (CompanyItemViewModel) -> Unit) = with(this.itemView) {
        this.setOnClickListener { listener(company) }
    }
}
