// Created by 超悟空 on 2022/6/2.

package com.latitech.whiteboard.example.playback

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.latitech.whiteboard.example.databinding.ActivityPlaybackBinding

/**
 * 回放界面
 */
class PlaybackActivity : AppCompatActivity() {
    companion object {

        private const val TAG = "PlaybackActivity"

        /**
         * 传递回放界面data
         */
        const val PLAYBACK_DATA_TAG = "playback_data_tag"
    }

    /**
     * 视图模型
     */
    private val viewModel by viewModels<PlaybackViewModel>()

    /**
     * 视图绑定
     */
    private val binding by lazy {
        ActivityPlaybackBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.playback.init(intent.getStringExtra(PLAYBACK_DATA_TAG)!!)
    }

    override fun onContentChanged() {
        binding.play.setOnClickListener {
            viewModel.playback.play()
        }

        binding.pause.setOnClickListener {
            viewModel.playback.pause()
        }

        binding.stop.setOnClickListener {
            viewModel.playback.stop()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                Log.v(TAG, "onStartTrackingTouch ${seekBar.progress}")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Log.v(TAG, "onStopTrackingTouch ${seekBar.progress}")
                viewModel.playback.seek(seekBar.progress)
            }
        })
    }
}