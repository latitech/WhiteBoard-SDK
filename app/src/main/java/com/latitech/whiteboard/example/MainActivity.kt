package com.latitech.whiteboard.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.latitech.whiteboard.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {

        /**
         * 北纬白板sdk的应用appId，需要替换为您自己申请的appId。
         */
        private const val SDK_APP_ID = ""

        /**
         * 北纬白板sdk的应用appSecret，需要替换为您自己申请的appSecret。
         */
        private const val SDK_APP_SECRET = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WhiteBoardSDK)
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
