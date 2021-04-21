package com.latitech.whiteboard.example

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter

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
}