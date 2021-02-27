package com.authguidance.basicmobileapp.views.companies

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.authguidance.basicmobileapp.databinding.CompanyListItemBinding

/*
 * An adapter to render company items in a custom manner
 * https://medium.com/@sanjeevy133/an-idiots-guide-to-android-recyclerview-and-databinding-4ebf8db0daff
 */
class CompanyArrayAdapter(
    val context: Context,
    val companies: List<CompanyItemViewModel>,
    val onClickListener: (CompanyItemViewModel) -> Unit
) : RecyclerView.Adapter<CompanyItemViewHolder>() {

    /*
     * Inflate this list item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyItemViewHolder {

        val inflater = LayoutInflater.from(this.context)
        val itemBinding = CompanyListItemBinding.inflate(inflater, parent, false)
        return CompanyItemViewHolder(itemBinding)
    }

    /*
     * Binds this item to the view
     */
    override fun onBindViewHolder(holder: CompanyItemViewHolder, position: Int) {

        val currentCompanyViewModel = this.companies[position]
        holder.bind(currentCompanyViewModel)
        holder.onClick(currentCompanyViewModel, this.onClickListener)
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.companies.size
    }
}
