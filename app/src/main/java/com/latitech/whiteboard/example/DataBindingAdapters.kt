package com.latitech.whiteboard.example

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

/**
 * 通用数据绑定适配器
 *
 * @author 超悟空
 * @version 1.0 2019/10/29
 * @since 1.0 2019/10/29
 **/
object DataBindingAdapters {

    /**
     * ConstraintLayout的layout_constraintDimensionRatio属性绑定
     */
    @JvmStatic
    @BindingAdapter("layout_constraintDimensionRatio")
    fun setConstraintDimensionRatio(view: View, ratio: String) {
        view.layoutParams.let {
            if (it is ConstraintLayout.LayoutParams) {
                it.dimensionRatio = ratio
                view.requestLayout()
            }
        }
    }

    /**
     * 设置图片网络源
     */
    @JvmStatic
    @BindingAdapter("srcUrl")
    fun setImageUrl(imageView: ImageView, url: String?) {
        Glide.with(imageView.context).load(url).into(imageView)
    }

    /**
     * 可见性属性
     */
    @JvmStatic
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
}