// Created by cwk on 2021/11/5.

package com.latitech.whiteboard.example

import androidx.lifecycle.ViewModel
import com.latitech.whiteboard.model.JoinConfig

/**
 * 首页功能
 */
class MainViewModel : ViewModel() {

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

    /**
     * 创建房间
     *
     * @return 邀请码
     */
    suspend fun createRoom() :String{
        return ""
    }

    /**
     * 获取房间配置信息
     *
     * @param code 邀请码
     */
    suspend fun getRoomConfig(code:String): JoinConfig {
        return JoinConfig(
            APP_ID,
            ROOM_ID,
            USER_ID,
            TOKEN
        )
    }
}