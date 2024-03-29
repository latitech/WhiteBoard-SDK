// Created by 超悟空 on 2018/3/15.

package com.latitech.whiteboard.example.room

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.example.*
import com.latitech.whiteboard.example.common.*
import com.latitech.whiteboard.example.databinding.ActivityRoomBinding
import com.latitech.whiteboard.model.FileConfig
import splitties.alertdialog.appcompat.alertDialog
import java.io.File
import java.util.Arrays

/**
 * 白板房间
 **/
class RoomActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "RoomActivity"

        /**
         * 传递房间data
         */
        const val ROOM_DATA_TAG = "room_data_tag"

        /**
         * 传递房间中所有bucketId数组
         */
        const val BUCKET_IDS_DATA_TAG = "bucket_ids_data_tag"
    }

    /**
     * 视图模型
     */
    private val viewModel by viewModels<RoomViewModel>()

    /**
     * 视图绑定
     */
    private val binding by lazy {
        ActivityRoomBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.bucketIds = intent.getStringArrayExtra(BUCKET_IDS_DATA_TAG)!!.toList()

        binding.pageList.adapter =
            PageListAdapter(this, viewModel.currentPage, viewModel.whiteBoardClient).apply {
                viewModel.pageList.observe(this@RoomActivity) {
                    submitList(it)
                }
            }

        binding.userList.adapter = UserListAdapter().apply {
            viewModel.userList.observe(this@RoomActivity) {
                submitList(it)
            }
        }

        WhiteBoard.joinRoom(intent.getParcelableExtra(ROOM_DATA_TAG)!!)
    }

    override fun onContentChanged() {

        binding.insertFile.setOnClickListener {
            insertFilePopupMenu.show()
        }

        binding.operation.setOnClickListener {
            viewModel.changeInputType(InputType.OPERATION)
        }

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
                        setView(ImageView(this@RoomActivity).apply {
                            setImageBitmap(it)
                            scaleType = ImageView.ScaleType.CENTER_INSIDE
                        })
                    }.show()
                }
            }
        }

        binding.deleteFile.setOnClickListener {
            viewModel.activeWidget.value?.let {
                viewModel.whiteBoardClient.deleteFile(it.id)
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

        binding.members.setOnClickListener {
            viewModel.memberListVisible.value = !viewModel.memberListVisible.value!!
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

        binding.preFilePage.setOnClickListener {
            viewModel.whiteBoardClient.prePage()
        }

        binding.nextFilePage.setOnClickListener {
            viewModel.whiteBoardClient.nextPage()
        }

        binding.preFileStep.setOnClickListener {
            viewModel.whiteBoardClient.preStep()
        }

        binding.nextFileStep.setOnClickListener {
            viewModel.whiteBoardClient.nextStep()
        }

        binding.switchBucket.setOnClickListener {
            alertDialog {
                setItems(Array(viewModel.bucketIds.size) { "白板${it + 1}" }) { _, which ->
                    WhiteBoard.switchBucket(viewModel.bucketIds[which])
                }
            }.show()
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

    /**
     * 插入文件/图片的选项弹窗
     */
    private val insertFilePopupMenu by lazy {
        PopupMenu(this, binding.insertFile).apply {
            inflate(R.menu.insert_file_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.camera -> openCamera()
                    R.id.gallery -> openGallery.launch("image/*")
                    R.id.file -> openFile.launch("*/*")
                }
                true
            }
        }
    }

    /**
     * 照相
     */
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.imageTempPath = FileUtil.createImagePath(this)
            takePicture.launch(FileUtil.createImageUri(this, viewModel.imageTempPath))
        } else {
            requestPermissionCamera.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * 拍照
     */
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        val file = File(viewModel.imageTempPath)
        if (file.exists()) {
            viewModel.whiteBoardClient.insertFile(FileConfig.Builder(file).build())
        }
    }

    /**
     * 请求相机权限
     */
    private val requestPermissionCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                openCamera()
            }
        }

    /**
     * 选择图片
     */
    private val openGallery = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            return@registerForActivityResult
        }

        val path = FileUtil.getPathFromUri(this, it)

        if (path != null) {
            Log.v(TAG, "openGallery path:$path")

            viewModel.whiteBoardClient.insertFile(FileConfig.Builder(File(path)).build())
        }
    }

    /**
     * 选择文件
     */
    private val openFile = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            return@registerForActivityResult
        }

        val path = FileUtil.getPathFromUri(this, it)

        if (path != null) {
            Log.v(TAG, "openFile path:$path")

            FileConfig.Builder(File(path))
                .location(
                    viewModel.whiteBoardClient.viewport.size.displayWidth / 5f,
                    viewModel.whiteBoardClient.viewport.size.displayHeight / 5f
                )
                .boxSize(800, 800)
                .build()
                .let(viewModel.whiteBoardClient::insertFile)
        }
    }
}