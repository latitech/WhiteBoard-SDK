package com.latitech.whiteboard.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.latitech.whiteboard.example.databinding.ActivityMainBinding
import com.latitech.whiteboard.model.JoinConfig
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    companion object {

        private const val APP_ID = "a4b26ecae3744e3fb60ff679e186cd98"

        private const val ROOM_ID = "399bbd57486b4a83b68485f353dd7153"

        private const val USER_ID = "0457987b-82f7-409d-8d58-24b42dda76f2"

        private const val TOKEN = "c0c9b8a3119190152dd5e6697eb78d6a"
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
            ).apply {
                roleId = 6
            }

            start<RoomActivity> {
                putExtra(RoomActivity.ROOM_DATA_TAG, params)
            }
        }
    }
}
