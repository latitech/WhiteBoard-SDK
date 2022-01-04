package com.latitech.whiteboard.example

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.latitech.whiteboard.example.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.toast.toast
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * 标记是否已经启动
         */
        private const val STARTED = "whiteboard_started"

        /**
         * 北纬白板sdk的应用appId，需要替换为您自己申请的appId。
         */
        private const val SDK_APP_ID = ""
    }

    /**
     * 视图模型
     */
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WhiteBoardSDK)
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState?.containsKey(STARTED) != true) {
            splashTransition()
        }

        binding.joinButton.setOnClickListener {
            joinRoom(binding.roomCodeInput.editableText.trim().toString())
        }

        binding.createCard.setOnClickListener {
            createRoom()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STARTED, true)
    }

    /**
     * 加入房间
     */
    private fun joinRoom(code: String) = lifecycleScope.launch {
        val params = try {
            viewModel.getRoomConfig(SDK_APP_ID, code)
        } catch (e: IOException) {
            toast("加入房间失败")
            return@launch
        }

        start<RoomActivity> {
            putExtra(RoomActivity.ROOM_DATA_TAG, params)
            putExtra(RoomActivity.ROOM_CODE_TAG, code)
        }
    }

    /**
     * 创建房间
     */
    private fun createRoom() = lifecycleScope.launch {
        val code = try {
            viewModel.createRoom(SDK_APP_ID)
        } catch (e: IOException) {
            toast("创建房间失败")
            return@launch
        }

        joinRoom(code)
    }

    /**
     * 启动页转换
     */
    private fun splashTransition() {
        ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_XY
            setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.launch_background,
                    theme
                )
            )
            addContentView(
                this,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            animate()
                .alpha(0.0f)
                .setDuration(1000)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationCancel(animation: Animator) {
                        onAnimationEnd(animation)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        (parent as ViewGroup).removeView(this@apply)
                    }
                })
        }
    }
}
