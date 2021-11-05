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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    /**
     * 视图模型
     */
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WhiteBoardSDK)
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        splashTransition()

        binding.joinButton.setOnClickListener {
            joinRoom(binding.roomCodeInput.editableText.trim().toString())
        }

        binding.createCard.setOnClickListener {
            createRoom()
        }
    }

    /**
     * 加入房间
     */
    private fun joinRoom(code: String) = lifecycleScope.launch {
        // 加入房间
        val params = viewModel.getRoomConfig(code)

        start<RoomActivity> {
            putExtra(RoomActivity.ROOM_DATA_TAG, params)
            putExtra(RoomActivity.ROOM_CODE_TAG, code)
        }
    }

    /**
     * 创建房间
     */
    private fun createRoom() = lifecycleScope.launch {
        val code = viewModel.createRoom()

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
