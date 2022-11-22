// Created by 超悟空 on 2018/10/18.

package com.latitech.whiteboard.example.offline

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.example.common.*
import com.latitech.whiteboard.listener.AutoRemoveWhiteBoardListener
import com.latitech.whiteboard.model.InputConfig
import com.latitech.whiteboard.model.WhiteBoardPage

/**
 * 离线模式功能
 **/
class OfflineViewModel : ViewModel() {

    /**
     * 白板控制器
     */
    val whiteBoardClient = WhiteBoard.createInstance()

    /**
     * 页信息列表
     */
    val pageList = MutableLiveData<List<WhiteBoardPage>>().apply {
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onBoardPageList(list: List<WhiteBoardPage>) {
                Log.i(TAG, "onBoardPageList count:${list.size}")
                value = list
            }

            override fun onBoardPageInfoChanged(page: WhiteBoardPage) {
                Log.i(TAG, "onBoardPageInfoChanged ${page.pageNumber}")
                value = WhiteBoard.getPageList()
            }
        })
    }

    /**
     * 当前白板页
     */
    val currentPage = MutableLiveData<WhiteBoardPage>().apply {
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onCurrentBoardPageChanged(page: WhiteBoardPage) {
                Log.i(TAG, "onCurrentBoardPageChanged ${page.pageNumber}")
                value = page
            }
        })
    }

    /**
     * 当前是否可还原笔迹
     */
    val canRecovery = MutableLiveData<Boolean>().apply {
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onRecoveryStateChanged(isEmpty: Boolean) {
                Log.i(TAG, "onRecoveryStateChanged")
                value = !isEmpty
            }
        })
    }

    /**
     * 当前白板背景主题
     */
    val theme = MutableLiveData(BoardTheme.white()).apply {
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onBackgroundColorChanged(backgroundColor: Int) {
                Log.i(TAG, "onBackgroundColorChanged")
                value = BoardTheme(backgroundColor)
            }
        })
    }

    /**
     * 页列表是否可见
     */
    val pageListVisible = MutableLiveData(false)

    /**
     * 设置功能区是否可见
     */
    val settingsVisible = MutableLiveData(false)

    /**
     * 工具条是否展开
     */
    val toolbarExpanded = MutableLiveData(true)

    /**
     * 普通笔配置
     */
    val normalPenStyle = NormalPenStyle(whiteBoardClient)

    /**
     * 马克笔配置
     */
    val markPenStyle = MarkPenStyle(whiteBoardClient)

    /**
     * 橡皮配置
     */
    val eraserStyle = EraserStyle(whiteBoardClient)

    /**
     * 激光笔配置
     */
    val laserStyle = LaserStyle(whiteBoardClient)

    /**
     * 几何图形样式
     */
    val geometryStyle = GeometryStyle(whiteBoardClient)

    /**
     * 当前输入模式
     */
    val currentInputType = MutableLiveData(InputType.NORMAL)

    init {
        // 进入离线模式，生成16:9的白板，可垂直滚动共三屏，初始背景色为浅灰色
        WhiteBoard.enterOffline(16f / 9, 3, 0xFFF5F5F5.toInt())
    }

    /**
     * 改变输入模式
     *
     * @param inputType 新的输入模式
     */
    fun changeInputType(inputType: InputType) {
        currentInputType.value = inputType
        val config = when (inputType) {
            InputType.NORMAL -> normalPenStyle.inputConfig
            InputType.MARK -> markPenStyle.inputConfig
            InputType.LASER -> laserStyle.inputConfig
            InputType.ERASE -> eraserStyle.inputConfig
            InputType.SELECT -> InputConfig.select()
            InputType.GEOMETRY -> geometryStyle.inputConfig
            InputType.OPERATION -> InputConfig.operation()
        }
        whiteBoardClient.setInputMode(config)
    }

    override fun onCleared() {
        WhiteBoard.exitOffline()
    }

    companion object {
        private const val TAG = "OfflineViewModel"
    }
}