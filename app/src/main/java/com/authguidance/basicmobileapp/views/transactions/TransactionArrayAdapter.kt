package com.authguidance.basicmobileapp.views.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.authguidance.basicmobileapp.databinding.TransactionListItemBinding

/*
 * An adapter to render transaction items
 */
class TransactionArrayAdapter(
    val context: Context,
    val transactions: List<TransactionItemViewModel>
) : RecyclerView.Adapter<TransactionItemViewHolder>() {

    /*
     * Inflate this list item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionItemViewHolder {

        val inflater = LayoutInflater.from(this.context)
        val itemBinding = TransactionListItemBinding.inflate(inflater, parent, false)
        return TransactionItemViewHolder(itemBinding)
    }

    /*
     * Binds an item to a view
     */
    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int) {

        val currentTransactionViewModel = this.transactions[position]
        holder.bind(currentTransactionViewModel)
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.transactions.size
    }
}
