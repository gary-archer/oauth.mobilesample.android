package com.authsamples.basicmobileapp.views.transactions

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.authsamples.basicmobileapp.databinding.TransactionListItemBinding

/*
 * An adapter to render transaction items in a recycler view
 */
class TransactionArrayAdapter(
    val context: Context,
    val model: TransactionsViewModel,
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

        val currentTransactionViewModel = TransactionItemViewModel(this.model.transactionsList[position])
        holder.bind(currentTransactionViewModel)
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.model.transactionsList.size
    }

    /*
     * Tell the adapter to reload its data from the model
     */
    @SuppressLint("NotifyDataSetChanged")
    fun reloadData() {
        this.notifyDataSetChanged()
    }
}
