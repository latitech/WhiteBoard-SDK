// Created by 超悟空 on 2022/6/2.

package com.latitech.whiteboard.example.playback

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.latitech.whiteboard.WhiteBoardPlayback
import com.latitech.whiteboard.listener.WhiteBoardPlaybackListener
import com.latitech.whiteboard.model.WhiteBoardSize
import com.latitech.whiteboard.type.BoardMode
import com.latitech.whiteboard.type.PlaybackStatus
import java.text.SimpleDateFormat

/**
 * 回放功能
 */
class PlaybackViewModel : ViewModel() {

    /**
     * 回放控制器
     */
    val playback = WhiteBoardPlayback.createInstance()

    /**
     * 时间格式化工具
     */
    @SuppressLint("SimpleDateFormat")
    val formatter = SimpleDateFormat("mm:ss")

    /**
     * 回放总时长，显示用
     */
    val totalTime = MutableLiveData("00")

    /**
     * 当前播放时长，显示用
     */
    val currentTime = MutableLiveData("00")

    /**
     * 回放总时长
     */
    val duration = MutableLiveData(0)

    /**
     * 当前播放时长
     */
    val position = MutableLiveData(0)

    /**
     * 回放状态
     */
    val status = MutableLiveData(PlaybackStatus.IDLE)

    init {
        playback.setListener(object : WhiteBoardPlaybackListener {
            override fun onStatusChanged(status: PlaybackStatus) {
                Log.v(TAG, "onStatusChanged $status")
                this@PlaybackViewModel.status.value = status
            }

            override fun onProgress(position: Int, duration: Int) {
                this@PlaybackViewModel.position.value = position
                this@PlaybackViewModel.currentTime.value = formatter.format(position)
            }

            override fun onInitFinished(duration: Int) {
                Log.v(TAG, "onInitFinished $duration")
                this@PlaybackViewModel.duration.value = duration
                this@PlaybackViewModel.totalTime.value = formatter.format(duration)
            }

            override fun onError(errorCode: Int) {
                Log.v(TAG, "onError $errorCode")
            }

            override fun onBoardSizeChanged(size: WhiteBoardSize) {
                Log.v(TAG, "onBoardSizeChanged ${size.displayWidth} * ${size.displayHeight}")
            }

            override fun onFileLoadingFailed(
                bucketId: String,
                mode: BoardMode?,
                extra: String?,
                errorCode: Int
            ) {
                Log.v(TAG, "onFileLoadingFailed $errorCode $mode $bucketId $extra")
            }
        })
    }

    override fun onCleared() {
        playback.release()
    }

    companion object {
        private const val TAG = "PlaybackViewModel"
    }
}