package com.example.raaga.landing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.raaga.data.Raga
import com.example.raaga.databinding.ListItemCurrentSamayBinding

class RagaListAdapter(val clickListener:RagaListener) : ListAdapter<Raga,RagaListAdapter.ViewHolder>(RagaListDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(getItem(position),clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent)
    }

    class ViewHolder(val binding: ListItemCurrentSamayBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Raga,
            clickListener: RagaListener
        ) {

            binding.bindingRaga = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemCurrentSamayBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class RagaListDiffCallback : DiffUtil.ItemCallback<Raga>(){
        override fun areItemsTheSame(oldItem: Raga, newItem: Raga): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Raga, newItem: Raga): Boolean {

            return oldItem == newItem
        }


    }
}
class RagaListener(val clickListener:(id:String?) -> Unit){

    fun onclick(raga:Raga) = clickListener(raga.id)
}