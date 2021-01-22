package com.example.raaga.prahars

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.raaga.data.Samay
import com.example.raaga.databinding.ListItemAllSamayBinding

class PraharsListAdapter(val clickListener: PraharsListener) : ListAdapter<Samay,PraharsListAdapter.ViewHolder>(PraharsListDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(clickListener,getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListItemAllSamayBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            clickListener: PraharsListener,
            item: Samay
        ) {
            binding.bindingSamay = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAllSamayBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class PraharsListDiffCallback : DiffUtil.ItemCallback<Samay>(){
        override fun areItemsTheSame(oldItem: Samay, newItem: Samay): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Samay, newItem: Samay): Boolean {

            return oldItem == newItem
        }

    }
}
class PraharsListener(val clickListener: (id:String) -> Unit){

    fun onClick(samay:Samay) = clickListener(samay.id)
}