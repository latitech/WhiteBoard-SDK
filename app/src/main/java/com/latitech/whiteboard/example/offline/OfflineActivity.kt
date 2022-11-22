// Created by 超悟空 on 2018/3/15.

package com.latitech.whiteboard.example.offline

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.latitech.whiteboard.example.common.*
import com.latitech.whiteboard.example.databinding.ActivityOfflineBinding
import splitties.alertdialog.appcompat.alertDialog

/**
 * 白板房间
 **/
class OfflineActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "OfflineActivity"
    }

    /**
     * 视图模型
     */
    private val viewModel by viewModels<OfflineViewModel>()

    /**
     * 视图绑定
     */
    private val binding by lazy {
        ActivityOfflineBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.pageList.adapter =
            PageListAdapter(this, viewModel.currentPage, viewModel.whiteBoardClient).apply {
                viewModel.pageList.observe(this@OfflineActivity) {
                    submitList(it)
                }
            }
    }

    override fun onContentChanged() {

        binding.select.setOnClickListener {
            viewModel.changeInputType(InputType.SELECT)
        }

        binding.restore.setOnClickListener {
            viewModel.whiteBoardClient.recover()
        }

        binding.pageMenu.setOnClickListener {
            viewModel.pageListVisible.value = !viewModel.pageListVisible.value!!
        }

        binding.prePage.setOnClickListener {
            viewModel.whiteBoardClient.preBoardPage()
        }

        binding.nextPage.setOnClickListener {
            viewModel.whiteBoardClient.nextBoardPage()
        }

        binding.newPage.setOnClickListener {
            viewModel.whiteBoardClient.newBoardPage()
        }

        binding.screenshots.setOnClickListener {
            viewModel.whiteBoardClient.screenshots {
                if (it == null) {
                    return@screenshots
                }
                Log.i(TAG, "screenshots ${it.width}x${it.height}")
                runOnUiThread {
                    alertDialog {
                        setView(ImageView(this@OfflineActivity).apply {
                            setImageBitmap(it)
                            scaleType = ImageView.ScaleType.CENTER_INSIDE
                        })
                    }.show()
                }
            }
        }

        binding.pen.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.NORMAL) {
                viewModel.changeInputType(InputType.NORMAL)
            } else {
                normalPenWindow.show(it)
            }
        }

        binding.mark.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.MARK) {
                viewModel.changeInputType(InputType.MARK)
            } else {
                markPenWindow.show(it)
            }
        }

        binding.eraser.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.ERASE) {
                viewModel.changeInputType(InputType.ERASE)
            } else {
                eraserWindow.show(it)
            }
        }

        binding.laser.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.LASER) {
                viewModel.changeInputType(InputType.LASER)
            } else {
                laserWindow.show(it)
            }
        }

        binding.geometry.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.GEOMETRY) {
                viewModel.changeInputType(InputType.GEOMETRY)
            } else {
                geometryWindow.show(it)
            }
        }

        binding.theme.setOnClickListener {
            themeWindow.show(it)
        }

        binding.shrink.setOnClickListener {
            viewModel.toolbarExpanded.value = false
        }

        binding.expand.setOnClickListener {
            viewModel.toolbarExpanded.value = true
        }

        binding.settings.setOnClickListener {
            viewModel.settingsVisible.value = !viewModel.settingsVisible.value!!
        }
    }

    /**
     * 创建普通笔颜色选择面板
     */
    private val normalPenWindow by lazy {
        PalettePopup(this).apply {
            addColorSelection(NormalPenStyle.colors, viewModel.normalPenStyle.colorIndex) {
                viewModel.normalPenStyle.colorIndex = it
            }
            addSizeSelection(NormalPenStyle.sizes, viewModel.normalPenStyle.sizeIndex) {
                viewModel.normalPenStyle.sizeIndex = it
            }
            addTextSelection(
                arrayOf("压感开", "压感关"),
                if (viewModel.normalPenStyle.supportPressure) 0 else 1
            ) {
                viewModel.normalPenStyle.supportPressure = it == 0
            }
        }
    }

    /**
     * 创建马克笔颜色选择面板
     */
    private val markPenWindow by lazy {
        PalettePopup(this).apply {
            addColorSelection(MarkPenStyle.colors, viewModel.markPenStyle.colorIndex) {
                viewModel.markPenStyle.colorIndex = it
            }
            addSizeSelection(MarkPenStyle.sizes, viewModel.markPenStyle.sizeIndex) {
                viewModel.markPenStyle.sizeIndex = it
            }
            addTextSelection(
                arrayOf("压感开", "压感关"),
                if (viewModel.markPenStyle.supportPressure) 0 else 1
            ) {
                viewModel.markPenStyle.supportPressure = it == 0
            }
        }
    }

    /**
     * 创建橡皮大小选择面板
     */
    private val eraserWindow by lazy {
        PalettePopup(this).apply {
            addSizeSelection(EraserStyle.sizes, viewModel.eraserStyle.sizeIndex) {
                viewModel.eraserStyle.sizeIndex = it
            }
        }
    }

    /**
     * 创建激光笔图形选择面板
     */
    private val laserWindow by lazy {
        PalettePopup(this).apply {
            addIconSelection(LaserStyle.icons.keys, viewModel.laserStyle.iconKey) {
                viewModel.laserStyle.iconKey = it
            }
        }
    }

    /**
     * 创建几何图形选择面板
     */
    private val geometryWindow by lazy {
        PalettePopup(this).apply {
            addIconSelection(GeometryStyle.icons.keys, viewModel.geometryStyle.iconKey) {
                viewModel.geometryStyle.iconKey = it
            }
            addColorSelection(GeometryStyle.colors, viewModel.geometryStyle.colorIndex) {
                viewModel.geometryStyle.colorIndex = it
            }
            addSizeSelection(GeometryStyle.sizes, viewModel.geometryStyle.sizeIndex) {
                viewModel.geometryStyle.sizeIndex = it
            }
        }
    }

    /**
     * 创建背景主题颜色选择面板
     */
    private val themeWindow by lazy {
        PalettePopup(this).apply {
            val colors = intArrayOf(
                BoardThemeType.WHITE.color(),
                BoardThemeType.BLACK.color(),
                BoardThemeType.GREEN.color(),
                BoardThemeType.TRANSLUCENT.color(),
            )

            addColorSelection(colors, viewModel.theme.value!!.themeType.ordinal) {
                viewModel.whiteBoardClient.backgroundColor = colors[it]
            }
        }
    }
}