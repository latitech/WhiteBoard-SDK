// Created by 超悟空 on 2021/4/16.

package com.latitech.whiteboard.example

import android.app.Application
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.example.common.NormalPenStyle
import com.latitech.whiteboard.model.InputConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        WhiteBoard.init(this, BuildConfig.DEBUG)

        WhiteBoard.setDefaultInputMode(
            InputConfig.pen(
                NormalPenStyle.colors[0],
                NormalPenStyle.sizes[0]
            )
        )
    }
}