// Created by cwk on 2021/11/5.

package com.latitech.whiteboard.example

import androidx.lifecycle.ViewModel
import com.latitech.whiteboard.model.JoinConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * 首页功能
 */
class MainViewModel : ViewModel() {

    companion object {

        /**
         * demo 使用的演示api接口地址
         */
        private const val BASE_URL = "https://sdktest.efaceboard.cn:8888/Chatboard"

        /**
         * 白板垂直方向延展倍数，从1开始。
         * 白板总高度为：单屏高度*延展倍数
         */
        private const val EXTENDS = 2

        /**
         * 白板宽高比
         */
        private const val ASPECT_RATIO = 0.5

        /**
         * 白板默认背景色枚举，1是浅灰，2是黑色，3是绿色
         */
        private const val BACKGROUND_COLOR = 1

        /**
         * application/json content-type
         */
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * okhttp
     */
    private val okHttpClient by lazy { OkHttpClient() }

    /**
     * 创建房间
     *
     * @return 邀请码
     *
     * @throws IOException 如果网络请求失败或接口执行失败
     */
    suspend fun createRoom(): String = withContext(Dispatchers.IO) {
        val body = """
            {
                "bgColor":$BACKGROUND_COLOR,
                "extendTimes":$EXTENDS,
                "widthHeightThan":$ASPECT_RATIO
            }
        """.toRequestBody(JSON)

        val request = Request.Builder()
            .url("$BASE_URL/board/create")
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("create room failed")

            val jsonObject = JSONObject(response.body!!.string())

            if (!jsonObject.getBoolean("state")) {
                throw IOException("create room failed")
            }

            jsonObject.getString("result")
        }
    }

    /**
     * 获取房间配置信息
     *
     * @param code 邀请码
     */
    suspend fun getRoomConfig(code: String): JoinConfig = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/board/getProperty".toHttpUrl().newBuilder()
            .addQueryParameter("inviteCode", code).build()

        val request = Request.Builder()
            .url(url)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("create room failed")

            val jsonObject = JSONObject(response.body!!.string())

            if (!jsonObject.getBoolean("state")) {
                throw IOException("create room failed")
            }

            jsonObject.getJSONObject("result").let {
                JoinConfig(
                    it.getString("appId"),
                    it.getString("meetingId"),
                    it.getString("userId"),
                    it.getString("token")
                )
            }
        }
    }
}