package com.latitech.whiteboard.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.latitech.whiteboard.example.databinding.ActivityMainBinding
import com.latitech.whiteboard.example.offline.OfflineActivity
import com.latitech.whiteboard.example.playback.PlaybackActivity
import com.latitech.whiteboard.example.room.RoomActivity
import com.latitech.whiteboard.model.JoinConfig
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    companion object {

        /**
         * 申请的SDK appId
         */
        private const val APP_ID = "8f31ad0d-79ac-46b3-8958-7c934a83f167"

        /**
         * 在房间中使用的用户id
         */
        private const val USER_ID = "test"

        /**
         * 加入时的房间用户token
         */
        private const val TOKEN = "ef5388b7ef0f298f8c5916ca53a823fd"

        /**
         * 加入的房间id
         */
        private const val ROOM_ID = "0009f86c56c9442ba2a463e76b55f044"

        /**
         * 房间中已经创建的白板bucketId
         * 如果房间只有自带的默认白板，没有添加更多白板或不需要体验白板切换功能，则可以留空
         */
        private val BUCKET_IDS = arrayOf(
            "8c886ada-e40b-4be0-b6ae-1a239c847f12",
            "44300bc0-4f3e-4664-bd2f-9a6bfe666c13",
            "7d3ed69b-4354-40d9-8571-ce54d8d73527",
        )

        /**
         * 查看回放的录制id
         */
        private val RECORD_ID = "0ee04e76-81df-48ca-8e9d-cd854c0ce77d"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.join.setOnClickListener {
            val params = JoinConfig(APP_ID, ROOM_ID, USER_ID, TOKEN)

            start<RoomActivity> {
                putExtra(RoomActivity.ROOM_DATA_TAG, params)
                putExtra(RoomActivity.BUCKET_IDS_DATA_TAG, BUCKET_IDS)
            }
        }

        binding.playback.setOnClickListener {
            start<PlaybackActivity> {
                putExtra(PlaybackActivity.PLAYBACK_DATA_TAG, RECORD_ID)
            }
        }

        binding.offline.setOnClickListener {
            start<OfflineActivity>()
        }
    }
}
