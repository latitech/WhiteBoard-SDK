// Created by 超悟空 on 2021/4/26.

package com.latitech.whiteboard.example.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.isNotEmpty
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.latitech.whiteboard.example.R
import com.latitech.whiteboard.example.databinding.PopupCardBinding
import splitties.dimensions.dip

/**
 * 通用调色盘弹窗，选择颜色和大小
 */
class PalettePopup(private val context: Context) {

    /**
     * 根布局
     */
    private val parent: ViewGroup

    /**
     * 弹窗
     */
    val popupWindow: PopupWindow

    init {
        val binding = PopupCardBinding.inflate(LayoutInflater.from(context))
        parent = binding.popupParent
        popupWindow = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isTouchable = true
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable())
        }
    }

    /**
     * 增加颜色选择面板
     *
     * @param colors 待选颜色
     * @param selected 初始选中的序号
     * @param changedListener 颜色选择事件参数为选中序号
     */
    fun addColorSelection(
        colors: IntArray,
        selected: Int,
        changedListener: (selected: Int) -> Unit
    ) {
        loadChipGroup().apply {
            colors.forEachIndexed { index, i ->
                loadChip(this).apply {
                    chipIconTint = ColorStateList.valueOf(i)
                    id = index
                }
            }
            check(selected)
            setOnCheckedChangeListener { _, checkedId ->
                changedListener(checkedId)
            }
        }
    }

    /**
     * 增加大小选择面板
     *
     * @param sizes 待选大小
     * @param selected 初始选中的序号
     * @param changedListener 大小选择事件参数为选中序号
     */
    fun addSizeSelection(
        sizes: FloatArray,
        selected: Int,
        changedListener: (selected: Int) -> Unit
    ) {
        loadChipGroup().apply {
            val minSize = context.dip(16)
            val weight = context.dip(16).toFloat() / sizes.size

            sizes.forEachIndexed { index, _ ->
                loadChip(this).apply {
                    chipIconSize = weight * index + minSize
                    id = index
                }
            }
            check(selected)
            setOnCheckedChangeListener { _, checkedId ->
                changedListener(checkedId)
            }
        }
    }

    /**
     * 增加图形选择面板
     *
     * @param icons 待选图形
     * @param selected 初始选中的图形资源id
     * @param changedListener 图形选择事件参数为选中的图形资源id
     */
    fun addIconSelection(icons: Set<Int>, selected: Int, changedListener: (selected: Int) -> Unit) {
        loadChipGroup().apply {
            icons.forEach {
                loadChip(this).apply {
                    setChipIconResource(it)
                    id = it
                }
            }
            check(selected)
            setOnCheckedChangeListener { _, checkedId ->
                changedListener(checkedId)
            }
        }
    }

    /**
     * 增加带有文字描述的选择面板
     *
     * @param texts 待选文本
     * @param selected 初始选中的文本序号
     * @param changedListener 文本选择事件参数为选中的文本序号
     */
    fun addTextSelection(
        texts: Array<String>,
        selected: Int,
        changedListener: (selected: Int) -> Unit
    ) {
        loadChipGroup().apply {
            texts.forEachIndexed { index, t ->
                loadChip(this).apply {
                    text = t
                    textEndPadding = context.dip(8f)
                    textStartPadding = context.dip(8f)
                    id = index
                }
            }
            check(selected)
            setOnCheckedChangeListener { _, checkedId ->
                changedListener(checkedId)
            }
        }
    }

    /**
     * 显示弹窗
     *
     * @param anchor 锚点
     */
    fun show(anchor: View) {
        popupWindow.showAsDropDown(anchor, 0, -anchor.height, Gravity.END or Gravity.TOP)
    }

    /**
     * 加载一个[ChipGroup]
     */
    private fun loadChipGroup(): ChipGroup {
        if (parent.isNotEmpty()) {
            parent.addView(
                View(context).apply {
                    setBackgroundColor(Color.DKGRAY)
                },
                ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(1))
            )
        }

        return LayoutInflater.from(context).inflate(R.layout.palette_chip_group, parent, false)
            .let {
                parent.addView(it)
                it as ChipGroup
            }
    }

    /**
     * 加载一个[Chip]
     */
    private fun loadChip(root: ViewGroup): Chip {
        return LayoutInflater.from(context).inflate(R.layout.palette_chip, root, false).let {
            root.addView(it)
            it as Chip
        }
    }
}