package com.latitech.whiteboard.example

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.listener.AutoRemoveWhiteBoardListener
import com.latitech.whiteboard.model.*
import com.latitech.whiteboard.type.BoardStatus
import com.latitech.whiteboard.type.WidgetType

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
                Log.i(TAG, "onCurrentBoardPageChanged ${page.pageNumber}")
                value = page
            }
        })
    }

    /**
     * 当前在线用户列表
     */
    val userList = MutableLiveData<List<RoomMember>>().apply {
        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onUserList(users: List<RoomMember>) {
                Log.i(TAG, "onUserList")
                value = users
            }

            override fun onUserJoin(user: RoomMember) {
                Log.i(TAG, "onUserJoin $user")
                value = WhiteBoard.getUsers()
            }

            override fun onUserLeave(user: RoomMember) {
                Log.i(TAG, "onUserLeave $user")
                value = WhiteBoard.getUsers()
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

    /**
     * 当前激活的widget，仅保留文件和图片类型
     */
    val activeWidget = MutableLiveData<ActiveWidgetInfo?>().apply {
        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onWidgetActive(info: ActiveWidgetInfo?) {
                Log.i(TAG, "onWidgetActive")
                value = info?.takeIf { it.type == WidgetType.FILE || it.type == WidgetType.IMAGE }
            }
        })
    }

    /**
     * 普通笔配置
     */
    val normalPenStyle = NormalPenStyle()

    /**
     * 马克笔配置
     */
    val markPenStyle = MarkPenStyle()

    /**
     * 橡皮配置
     */
    val eraserStyle = EraserStyle()

    /**
     * 激光笔配置
     */
    val laserStyle = LaserStyle()

    /**
     * 几何图形样式
     */
    val geometryStyle = GeometryStyle()

    /**
     * 当前输入模式
     */
    var currentInputType = MutableLiveData(InputType.NORMAL)

    /**
     * 临时图片路径（拍照用）
     */
    var imageTempPath = ""

    init {

        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onJoinSuccess(room: Room, me: RoomMember) {
                Log.i(TAG, "onJoinSuccess")
            }

            override fun onJoinFailed(errorCode: Int) {
                Log.i(TAG, "onJoinFailed $errorCode")
            }

            override fun onReconnecting(times: Int) {
                Log.i(TAG, "onReconnecting $times")
            }

            override fun onReconnected() {
                Log.i(TAG, "onReconnected")
            }

            override fun onDisconnected() {
                Log.i(TAG, "onDisconnected")
            }

            override fun onBoardStatusChanged(status: BoardStatus) {
                Log.i(TAG, "onBoardStatusChanged $status")
            }

            override fun onBackgroundColorChanged(backgroundColor: Int) {
                Log.i(TAG, "onBackgroundColorChanged")
            }

            override fun onFilePageChanged(info: ActiveWidgetInfo) {
                Log.i(TAG, "onFilePageChanged $info")
            }

            override fun onWidgetActionEvent(event: WidgetActionEvent) {
                Log.i(TAG, "onWidgetActionEvent $event")
            }
        })
    }

    /**
     * 改变输入模式
     *
     * @param inputType 新的输入模式
     */
    fun changeInputType(inputType: InputType) {
        currentInputType.value = inputType
        when (inputType) {
            InputType.NORMAL -> WhiteBoard.setInputMode(normalPenStyle.inputConfig)
            InputType.MARK -> WhiteBoard.setInputMode(markPenStyle.inputConfig)
            InputType.LASER -> WhiteBoard.setInputMode(laserStyle.inputConfig)
            InputType.ERASE -> WhiteBoard.setInputMode(eraserStyle.inputConfig)
            InputType.SELECT -> WhiteBoard.setInputMode(InputConfig.select())
            InputType.GEOMETRY -> WhiteBoard.setInputMode(geometryStyle.inputConfig)
        }
    }

    override fun onCleared() {
        WhiteBoard.leaveRoom()
    }

    companion object {
        private const val TAG = "RoomViewModel"
    }
}