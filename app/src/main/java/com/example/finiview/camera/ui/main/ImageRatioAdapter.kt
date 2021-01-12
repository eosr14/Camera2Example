package com.example.finiview.camera.ui.main

import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.finiview.camera.BR
import com.example.finiview.camera.R
import com.example.finiview.camera.common.event.OnClickImageRatioEvent
import com.example.finiview.camera.common.event.RxEventBus
import com.example.finiview.camera.databinding.ItemImageRatioBinding

class ImageRatioAdapter : RecyclerView.Adapter<ImageRatioAdapter.MainViewHolder>() {

    private var items = ArrayList<Size>()

    fun setItems(items: ArrayList<Size>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun pushItems(items: ArrayList<Size>) {
        this.items.addAll(items)
        notifyItemInserted(this.items.size)
    }

    fun updateItems(items: ArrayList<Size>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_ratio, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.onBindViewHolder(items[position], position)
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = DataBindingUtil.bind<ItemImageRatioBinding>(itemView)

        fun onBindViewHolder(item: Size, position: Int) {
            itemView.tag = position

            binding?.apply {
                setVariable(BR.size, item)
                executePendingBindings()
            }

            itemView.setOnClickListener {
                RxEventBus.sendEvent(OnClickImageRatioEvent(item.width, item.height))
            }
        }
    }

}