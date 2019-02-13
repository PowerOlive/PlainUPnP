package com.m3sv.plainupnp.presentation.main

import androidx.databinding.ViewDataBinding
import androidx.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.ItemsDiffCallback
import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.databinding.GalleryContentFolderItemBinding
import com.m3sv.plainupnp.databinding.GalleryContentItemBinding
import com.m3sv.plainupnp.presentation.base.BaseAdapter
import com.m3sv.plainupnp.presentation.base.ItemViewHolder
import com.m3sv.plainupnp.presentation.main.data.ContentType
import com.m3sv.plainupnp.presentation.main.data.Item


interface OnItemClickListener {
    fun onDirectoryClick(directoryName: String, itemUri: String?, parentId: String?)

    fun onItemClick(item: DIDLItem, position: Int)
}

class GalleryContentAdapter(private val glide: RequestManager, private val onItemClickListener: OnItemClickListener) :
    BaseAdapter<Item>(GalleryContentAdapter.diffCallback) {

    var clickable = true

    private val emptyRequestOptions = RequestOptions()

    private val audioRequestOptions = RequestOptions().placeholder(R.drawable.ic_music_note)

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder<ViewDataBinding> = when (ContentType.values()[viewType]) {
        ContentType.DIRECTORY -> ItemViewHolder(
            GalleryContentFolderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        else -> ItemViewHolder(
            GalleryContentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder<ViewDataBinding>, position: Int) {
        val item = items[position]

        val itemClickListener = View.OnClickListener {
            if (clickable)
                holder.adapterPosition.takeIf { it >= 0 }?.let { adapterPosition ->
                    item.didlObjectDisplay?.get(adapterPosition)?.let {
                        onItemClickListener.onItemClick(
                            it.didlObject as DIDLItem,
                            holder.adapterPosition
                        )
                    }
                }
        }

        when (item.type) {
            ContentType.IMAGE -> loadData(
                holder,
                item,
                R.drawable.ic_image,
                itemClickListener,
                emptyRequestOptions
            )
            ContentType.VIDEO -> loadData(
                holder,
                item,
                R.drawable.ic_video,
                itemClickListener,
                emptyRequestOptions
            )
            ContentType.AUDIO -> loadData(
                holder, item, R.drawable.ic_music, itemClickListener, audioRequestOptions
            )

            ContentType.DIRECTORY -> loadDirectory(holder, item)
        }
    }

    private fun <T : ViewDataBinding> ItemViewHolder<*>.extractBinding(): T =
        (this as ItemViewHolder<T>).binding

    private fun loadData(
        holder: ItemViewHolder<ViewDataBinding>,
        item: Item,
        @DrawableRes contentTypeIcon: Int,
        onClick: View.OnClickListener,
        requestOptions: RequestOptions
    ) {
        with(holder.extractBinding<GalleryContentItemBinding>()) {
            Glide.with(holder.itemView)
                .load(item.uri)
                .thumbnail(0.1f)
                .apply(requestOptions)
                .into(thumbnail)

            title.text = item.name

            container.setOnClickListener(onClick)
            contentType.setImageResource(contentTypeIcon)
            contentType.setOnClickListener(onClick)
        }
    }

    private fun loadDirectory(
        holder: ItemViewHolder<*>,
        item: Item
    ) {
        with(holder.extractBinding<GalleryContentFolderItemBinding>()) {
            title.text = item.name
            thumbnail.setImageResource(R.drawable.ic_folder)
            container.setOnClickListener {
                if (clickable)
                    onItemClickListener.onDirectoryClick(item.name, item.uri, item.parentId)
            }
        }
    }

    fun filter(text: CharSequence) {
        if (text.isEmpty()) {
            resetItems()
            return
        }

        filterWithDiff { it.name.toLowerCase().contains(text) }
    }


    class DiffCallback(
        oldItems: List<Item>,
        newItems: List<Item>
    ) :
        ItemsDiffCallback<Item>(oldItems, newItems) {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].uri == newItems[newItemPosition].uri
        }
    }

    companion object {
        val diffCallback = DiffCallback(listOf(), listOf())
    }
}