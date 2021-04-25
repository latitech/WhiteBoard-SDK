package com.latitech.whiteboard.example

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.listener.AutoRemoveWhiteBoardListener
import com.latitech.whiteboard.model.*
import com.latitech.whiteboard.type.BoardStatus

/**
 * 房间功能
 *
 * @author 超悟空
 * @version 1.0 2018/10/18
 * @since 1.0 2018/10/18
 **/
class RoomViewModel : ViewModel() {

    /**
     * 白板宽高比
     */
    val whiteBoardRatio = MutableLiveData("2048:1440").apply {
        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onBoardSizeChanged(viewport: WhiteBoardViewport) {
                Log.i(TAG, "onBoardSizeChanged")
                value = "${viewport.size.displayWidth}:${viewport.size.displayHeight}"
            }
        })
    }

    /**
     * 页信息列表
     */
    val pageList = MutableLiveData<List<WhiteBoardPage>>().apply {
        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
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
        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onCurrentBoardPageChanged(page: WhiteBoardPage) {
                Log.i(TAG, "onCurrentBoardPageChanged")
                value = page
            }
        })
    }

    /**
     * 当前是否可还原笔迹
     */
    val canRecovery = MutableLiveData<Boolean>().apply {
        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onRecoveryStateChanged(isEmpty: Boolean) {
                Log.i(TAG, "onRecoveryStateChanged")
                value = !isEmpty
            }
        })
    }

    init {

        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onJoinSuccess(room: Room, me: RoomMember) {
                Log.i(TAG, "onJoinSuccess")
            }

            override fun onJoinFailed(errorCode: Int) {
                Log.i(TAG, "onJoinFailed $errorCode")
            }

            override fun onReconnecting(time: Int) {
                Log.i(TAG, "onReconnecting")
            }

            override fun onReconnected() {
                Log.i(TAG, "onReconnected")
            }

            override fun onDisconnected() {
                Log.i(TAG, "onDisconnected")
            }

            override fun onBoardStatusChanged(status: BoardStatus) {
                Log.i(TAG, "onBoardStatusChanged")
            }

            override fun onUserList(users: List<RoomMember>) {
                Log.i(TAG, "onUserList")
            }

            override fun onUserJoin(user: RoomMember) {
                Log.i(TAG, "onUserJoin")
            }

            override fun onUserLeave(user: RoomMember) {
                Log.i(TAG, "onUserLeave")
            }

            override fun onBackgroundColorChanged(backgroundColor: Int) {
                Log.i(TAG, "onBackgroundColorChanged")
            }

            override fun onWidgetActive(info: ActiveWidgetInfo?) {
                Log.i(TAG, "onWidgetActive")
            }

            override fun onFilePageChanged(info: ActiveWidgetInfo) {
                Log.i(TAG, "onFilePageChanged")
            }

            override fun onWidgetActionEvent(event: WidgetActionEvent) {
                Log.i(TAG, "onWidgetActionEvent")
            }
        })
    }

    override fun onCleared() {
        WhiteBoard.leaveRoom()
    }

    companion object {
        private const val TAG = "RoomViewModel"
    }
}