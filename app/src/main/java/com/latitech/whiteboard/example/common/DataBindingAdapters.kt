// Created by 超悟空 on 2018/3/15.

package com.latitech.whiteboard.example.common

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.latitech.whiteboard.example.R

/**
 * 设置图片网络源
 */
@BindingAdapter("srcUrl")
fun ImageView.setImageUrl(url: String?) {
    Glide.with(context).load(url).into(this)
}

/**
 * 设置头像
 */
@BindingAdapter("avatar")
fun ImageView.setAvatar(url: String?) {
    Glide.with(context)
        .load(url)
        .circleCrop()
        .error(R.drawable.ic_baseline_account_circle_24)
        .placeholder(R.drawable.ic_baseline_account_circle_24)
        .into(this)
}

/**
 * 可见性属性
 */
@BindingAdapter("visibleGone")
fun View.showHide(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}