package com.latitech.whiteboard.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.latitech.whiteboard.example.databinding.ActivityMainBinding
import com.latitech.whiteboard.model.JoinConfig
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    companion object {

        private const val APP_ID = "a4b26ecae3744e3fb60ff679e186cd98"

        private const val ROOM_ID = "0f6d3faba9104b4e84c26edb706e10f0"

        private const val USER_ID = "c801e228-a612-4524-97e9-5829f3cf842e"

        private const val TOKEN = "dd250b965a9aa6eeeab023d14c5dc698"
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

            startActivity<RoomActivity>(RoomActivity.ROOM_DATA_TAG to params)
        }
    }
}
