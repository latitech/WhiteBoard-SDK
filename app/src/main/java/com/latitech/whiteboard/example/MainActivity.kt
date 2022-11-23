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
        private const val APP_ID = "a4b26ecae3744e3fb60ff679e186cd98"

        /**
         * 在房间中使用的用户id
         */
        private const val USER_ID = "test"

        /**
         * 加入时的房间用户token
         */
        private const val TOKEN = "3f808f3953cad85efca86a4c398b9a12"

        /**
         * 加入的房间id
         */
        private const val ROOM_ID = "b1b4c08a254a4488b15749df23e27356"

        /**
         * 房间中已经创建的白板bucketId
         * 如果房间只有自带的默认白板，没有添加更多白板或不需要体验白板切换功能，则可以留空
         */
        private val BUCKET_IDS = arrayOf(
            "76f5e1b7-2bc4-449a-86e3-4662f536b716", // 普通白板
            "59456d75-e35d-4e6b-84aa-f1d2d441cf7f", // pdf模式
            "b1e269ff-a229-4a30-9596-33d8ee63785c", // ppd模式
        )

        /**
         * 查看回放的录制id
         */
        private val RECORD_ID = "c1d8ef70-3951-402f-bc33-cf10d5ad604e"
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
