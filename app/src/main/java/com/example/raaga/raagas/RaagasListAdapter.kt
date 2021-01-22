package com.example.raaga.raagas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.raaga.data.Raga
import com.example.raaga.databinding.ListItemGivenSamayBinding

class RagasListAdapter(val clickListener:RagasListener) : ListAdapter<Raga, RagasListAdapter.ViewHolder>(RagasListDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(getItem(position),clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent)
    }

    class ViewHolder(val binding: ListItemGivenSamayBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Raga,
            clickListener: RagasListener
        ) {

            binding.bindingRaga1 = item
            binding.clickListener1 = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemGivenSamayBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class RagasListDiffCallback : DiffUtil.ItemCallback<Raga>(){
        override fun areItemsTheSame(oldItem: Raga, newItem: Raga): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Raga, newItem: Raga): Boolean {

            return oldItem == newItem
        }


    }
}
class RagasListener(val clickListener:(id:String) -> Unit){

    fun onclick(raga: Raga) = clickListener(raga.id)
}