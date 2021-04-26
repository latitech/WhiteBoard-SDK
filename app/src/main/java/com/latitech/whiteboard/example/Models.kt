// Created by 超悟空 on 2021/4/26.

package com.latitech.whiteboard.example

import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.model.InputConfig
import com.latitech.whiteboard.type.GeometryType
import com.latitech.whiteboard.type.LaserType

/**
 * 普通笔配置
 */
class NormalPenStyle {

    /**
     * 当前使用的颜色序号
     */
    var colorIndex = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的粗细序号
     */
    var sizeIndex = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.pen(colors[colorIndex], sizes[sizeIndex])

    companion object {
        /**
         * 可选颜色
         */
        val colors = intArrayOf(
                0xFF000000.toInt(),
                0xFFFFA726.toInt(),
                0xFFFFFF00.toInt(),
                0xFF388E3C.toInt(),
                0xFF2962FF.toInt(),
                0xFFD500F9.toInt(),
                0xFFFF0000.toInt(),
                0xFFFFFFFF.toInt(),
                0xFF8D6E63.toInt(),
        )

        /**
         * 可选粗细
         */
        val sizes = floatArrayOf(
                0.5f,
                1f,
                3f,
                5f,
                8f,
        )
    }
}

/**
 * 马克笔配置
 */
class MarkPenStyle {

    /**
     * 当前使用的颜色序号
     */
    var colorIndex = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的粗细序号
     */
    var sizeIndex = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.pen(colors[colorIndex], sizes[sizeIndex])

    companion object {
        /**
         * 可选颜色
         */
        val colors = intArrayOf(
                0x6F03DAC5,
                0x6FFFFF00,
                0x6FFF0000,
                0x6F2962FF,
                0x6FBB86FC,
        )

        /**
         * 可选粗细
         */
        val sizes = floatArrayOf(
                5f,
                10f,
                15f,
                20f,
                25f,
                30f,
        )
    }
}

/**
 * 橡皮配置
 */
class EraserStyle {

    /**
     * 当前使用的面积序号
     */
    var sizeIndex = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.erase(sizes[sizeIndex])

    companion object {

        /**
         * 可选面积（在白板虚拟尺寸中的大小）
         */
        val sizes = floatArrayOf(
                60f,
                100f,
                160f,
                240f,
        )
    }
}

/**
 * 激光笔样式
 */
class LaserStyle {
    /**
     * 当前使用的图形res（作为key）
     */
    var iconKey = R.drawable.ic_baseline_adjust_24
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.laserPen(icons[iconKey]!!)

    companion object {

        /**
         * 可选图形
         */
        val icons = mapOf(
                R.drawable.ic_baseline_adjust_24 to LaserType.LASER_DOT,
                R.drawable.ic_outline_pan_tool_24 to LaserType.LASER_HAND,
                R.drawable.ic_outline_near_me_24 to LaserType.LASER_ARROWS_WHITE,
                R.drawable.ic_baseline_north_west_24 to LaserType.LASER_ARROWS_BLACK,
        )
    }
}

/**
 * 几何图形样式
 */
class GeometryStyle {


    /**
     * 当前使用的图形res（作为key）
     */
    var iconKey = R.drawable.ic_outline_crop_landscape_24
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的颜色序号
     */
    var colorIndex = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的粗细序号
     */
    var sizeIndex = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            WhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.geometry(icons[iconKey]!!, colors[colorIndex], sizes[sizeIndex])

    companion object {

        /**
         * 可选图形
         */
        val icons = mapOf(
                R.drawable.ic_outline_crop_landscape_24 to GeometryType.RECTANGLE,
                R.drawable.ic_outline_brightness_1_24 to GeometryType.CIRCLE,
                R.drawable.ic_baseline_north_west_24 to GeometryType.ARROW,
                R.drawable.ic_baseline_horizontal_rule_24 to GeometryType.LINE,
        )

        /**
         * 可选颜色
         */
        val colors = intArrayOf(
                0xFF000000.toInt(),
                0xFFFFFF00.toInt(),
                0xFF388E3C.toInt(),
                0xFFD500F9.toInt(),
                0xFFFF0000.toInt(),
        )

        /**
         * 可选粗细
         */
        val sizes = floatArrayOf(
                1f,
                3f,
                5f,
                8f,
        )
    }
}

/**
 * 输入模式
 */
enum class InputType {
    /**
     * 普通笔
     */
    NORMAL,

    /**
     * 马克笔
     */
    MARK,

    /**
     * 激光笔
     */
    LASER,

    /**
     * 橡皮
     */
    ERASE,

    /**
     * 选择
     */
    SELECT,

    /**
     * 几何图形
     */
    GEOMETRY,
}