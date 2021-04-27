// Created by 超悟空 on 2021/4/27.

package com.latitech.whiteboard.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.latitech.whiteboard.example.databinding.ItemUserBinding
import com.latitech.whiteboard.model.RoomMember

/**
 * 用户列表适配器
 */
class UserListAdapter : ListAdapter<RoomMember, UserViewHolder>(UserDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UserViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
    )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.binding.apply {
            user = getItem(position)
            executePendingBindings()
        }
    }
}


class UserViewHolder(
    itemView: View,
    val binding: ItemUserBinding = ItemUserBinding.bind(itemView)
) : RecyclerView.ViewHolder(itemView)

object UserDiffCallback : DiffUtil.ItemCallback<RoomMember>() {
    override fun areItemsTheSame(oldItem: RoomMember, newItem: RoomMember) =
        oldItem.sessionId == newItem.sessionId

    override fun areContentsTheSame(oldItem: RoomMember, newItem: RoomMember) = oldItem == newItem
}