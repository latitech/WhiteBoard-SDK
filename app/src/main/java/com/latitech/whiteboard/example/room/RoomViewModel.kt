// Created by 超悟空 on 2018/10/18.

package com.latitech.whiteboard.example.room

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.example.common.*
import com.latitech.whiteboard.listener.AutoRemoveWhiteBoardListener
import com.latitech.whiteboard.model.*
import com.latitech.whiteboard.type.BoardMode
import com.latitech.whiteboard.type.BoardStatus
import com.latitech.whiteboard.type.RoomStatus
import com.latitech.whiteboard.type.WidgetType

/**
 * 房间功能
 **/
class RoomViewModel : ViewModel() {

    /**
     * 白板控制器
     */
    val whiteBoardClient = WhiteBoard.createInstance()

    /**
     * 当前房间状态
     */
    val roomStatus = MutableLiveData(WhiteBoard.getRoomStatus()).apply {
        WhiteBoard.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onRoomStatusChanged(status: RoomStatus) {
                Log.i(TAG, "onBoardStatusChanged $status")
                value = status
            }
        })
    }

    /**
     * 白板状态
     */
    val boardStatus = MutableLiveData(whiteBoardClient.status).apply {
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onBoardStatusChanged(status: BoardStatus) {
                Log.i(TAG, "onBoardStatusChanged $status")
                value = status
            }
        })
    }

    /**
     * 白板模式
     */
    val boardMode = MutableLiveData(BoardMode.NORMAL).apply {
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onWhiteBoardOpened(bucketId: String, mode: BoardMode) {
                value = mode
            }
        })
    }

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
     * ppt当前页码显示
     */
    val pptPages = MutableLiveData("0/0").apply {
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onFileStateChanged(data: MutableMap<String, Any>) {
                value = "${data["no"]}/${data["pageCount"]}"
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
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
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
        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onWidgetActive(info: ActiveWidgetInfo?) {
                Log.i(TAG, "onWidgetActive")
                value = info?.takeIf { it.type == WidgetType.FILE || it.type == WidgetType.IMAGE }
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
     * 成员列表是否可见
     */
    val memberListVisible = MutableLiveData(false)

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

            override fun onReconnecting(time: Int) {
                Log.i(TAG, "onReconnecting")
            }

            override fun onReconnected() {
                Log.i(TAG, "onReconnected")
            }

            override fun onDisconnected() {
                Log.i(TAG, "onDisconnected")
            }
        })

        whiteBoardClient.addListener(object : AutoRemoveWhiteBoardListener {
            override fun onWhiteBoardOpened(bucketId: String, mode: BoardMode) {
                Log.i(TAG, "onWhiteBoardOpened $mode")
            }

            override fun onWhiteBoardOpenFailed(bucketId: String, errorCode: Int) {
                Log.i(TAG, "onWhiteBoardOpenFailed $errorCode")
            }

            override fun onWhiteBoardClosed(bucketId: String) {
                Log.i(TAG, "onWhiteBoardClosed")
            }

            override fun onFileLoadedSuccessful() {
                Log.i(TAG, "onFileLoadedSuccessful")
            }

            override fun onFileLoadingFailed(errorCode: Int) {
                Log.i(TAG, "onFileLoadingFailed $errorCode")
            }

            override fun onMessage(command: String, content: String, sessionId: String) {
                Log.i(TAG, "onMessage command:$command content:$content sessionId:$sessionId")
            }

            override fun onFileStateChanged(data: MutableMap<String, Any>) {
                Log.i(TAG, "onFileStateChanged $data")
            }

            override fun onWidgetActionEvent(event: WidgetActionEvent) {
                Log.i(TAG, "onWidgetActionEvent $event")
            }

            override fun onFileScrolled(info: WidgetScrollInfo) {
                Log.i(TAG, "onWidgetScrolled top:${info.atTop} bottom:${info.atBottom} ")
            }

            override fun onPageCleaned(pageId: String) {
                Log.i(TAG, "page cleaned")
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
        WhiteBoard.leaveRoom()
    }

    companion object {
        private const val TAG = "RoomViewModel"
    }
}