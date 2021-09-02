package com.latitech.whiteboard.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.latitech.whiteboard.example.databinding.ActivityMainBinding
import com.latitech.whiteboard.model.JoinConfig
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    companion object {

        // 正式服务器测试号

        private const val APP_ID = "a4b26ecae3744e3fb60ff679e186cd98"

        private const val ROOM_ID = "32f13181ef444be1b5d2ad0f95db2432"

        private const val USER_ID = "test"

        private const val TOKEN = "b4f475ed67f3005aa733afc2784cdd0c"

        // 测试服务器测试号

//        private const val APP_ID = "a4b26ecae3744e3fb60ff679e186cd98"
//
//        private const val ROOM_ID = "d8471edb7b364744941f60eba4df2887"
//
//        private const val USER_ID = "test"
//
//        private const val TOKEN = "c63987b65858dcdf38a7611d9c6fcd77"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.join.setOnClickListener {
            // 加入房间
            val params = JoinConfig(
                APP_ID,
                ROOM_ID,
                USER_ID,
                TOKEN
            )

            start<RoomActivity> {
                putExtra(RoomActivity.ROOM_DATA_TAG, params)
            }
        }
    }
}
