// Created by 超悟空 on 2021/4/23.

package com.latitech.whiteboard.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.latitech.whiteboard.example.databinding.ItemPageNavigationBinding
import com.latitech.whiteboard.model.WhiteBoardPage

/**
 * 白板页列表适配器
 *
 * @property lifecycleOwner 生命周期
 * @property current 当前页
 */
class PageListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val current: LiveData<WhiteBoardPage>
) : ListAdapter<WhiteBoardPage, PageViewHolder>(PageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_page_navigation, parent, false),
            current
        ).apply {
            binding.lifecycleOwner = lifecycleOwner
        }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.page.value = getItem(position)
    }
}

class PageViewHolder(itemView: View, current: LiveData<WhiteBoardPage>) :
    RecyclerView.ViewHolder(itemView) {
    val binding = ItemPageNavigationBinding.bind(itemView)

    val page = MutableLiveData<WhiteBoardPage>()

    init {
        binding.isCurrentPage = page.switchMap {
            current.map {
                it.pageId == page.value?.pageId
            }
        }

        binding.page = page
    }
}

object PageDiffCallback : DiffUtil.ItemCallback<WhiteBoardPage>() {
    override fun areItemsTheSame(oldItem: WhiteBoardPage, newItem: WhiteBoardPage) =
        oldItem.pageId == newItem.pageId

    override fun areContentsTheSame(oldItem: WhiteBoardPage, newItem: WhiteBoardPage) =
        oldItem == newItem
}