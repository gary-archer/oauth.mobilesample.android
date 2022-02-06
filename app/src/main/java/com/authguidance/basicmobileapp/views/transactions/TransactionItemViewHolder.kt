package com.authguidance.basicmobileapp.views.transactions

import androidx.recyclerview.widget.RecyclerView
import com.authguidance.basicmobileapp.databinding.TransactionListItemBinding

/*
 * A custom view holder class for transaction rows in a recycler view
 */
class TransactionItemViewHolder(
    val layoutBinding: TransactionListItemBinding
) : RecyclerView.ViewHolder(
    layoutBinding.root
) {

    /*
     * Bind a data item to a layout row
     */
    fun bind(item: TransactionItemViewModel) {
        this.layoutBinding.model = item
        this.layoutBinding.executePendingBindings()
    }
}
