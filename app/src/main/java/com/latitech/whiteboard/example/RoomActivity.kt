package com.latitech.whiteboard.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.example.databinding.ActivityRoomBinding
import com.latitech.whiteboard.model.FileConfig
import com.latitech.whiteboard.model.InputConfig
import com.latitech.whiteboard.type.WidgetType
import org.jetbrains.anko.alert
import java.io.File

/**
 * 白板房间
 *
 * @author 超悟空
 * @version 1.0 2018/3/15
 * @since 1.0 2018/3/15
 **/
class RoomActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "RoomActivity"

        /**
         * 传递房间data
         */
        const val ROOM_DATA_TAG = "room_data_tag"
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

        binding.pageList.adapter = PageListAdapter(this, viewModel.currentPage).apply {
            viewModel.pageList.observe(this@RoomActivity) {
                if (it != null) {
                    submitList(it)
                }
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

        binding.whiteBoard.setZOrderMediaOverlay(true)

        binding.insertImage.setOnClickListener {
            openGallery.launch("image/*")
        }

        binding.camera.setOnClickListener {
            openCamera()
        }

        binding.insertFile.setOnClickListener {
            openFile.launch("*/*")
        }

        binding.select.setOnClickListener {
            WhiteBoard.setInputMode(InputConfig.select())
        }

        binding.restore.setOnClickListener {
            WhiteBoard.recover()
        }

        binding.prePage.setOnClickListener {
            WhiteBoard.preBoardPage()
        }

        binding.nextPage.setOnClickListener {
            WhiteBoard.nextBoardPage()
        }

        binding.newPage.setOnClickListener {
            WhiteBoard.newBoardPage()
        }

        binding.screenshots.setOnClickListener {
            WhiteBoard.screenshots {
                if (it == null) {
                    return@screenshots
                }

                runOnUiThread {
                    alert {
                        customView = ImageView(this@RoomActivity).apply {
                            setImageBitmap(it)
                            scaleType = ImageView.ScaleType.FIT_XY
                        }

                        show()
                    }
                }

            }
        }

        binding.deleteFile.setOnClickListener {
            viewModel.activeWidget.value?.let {
                WhiteBoard.deleteFile(it.id)
            }
        }

        binding.preFilePage.setOnClickListener {
            viewModel.activeWidget.value?.takeIf { it.type == WidgetType.FILE && it.currentPageNumber > 1 }
                ?.let {
                    WhiteBoard.jumpFilePage(it.id, it.currentPageNumber - 1)
                }
        }

        binding.nextFilePage.setOnClickListener {
            viewModel.activeWidget.value?.takeIf { it.type == WidgetType.FILE && it.currentPageNumber < it.pageCount }
                ?.let {
                    WhiteBoard.jumpFilePage(it.id, it.currentPageNumber + 1)
                }
        }

        val normalPenWindow = normalPenWindow()

        binding.pen.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.NORMAL) {
                viewModel.changeInputType(InputType.NORMAL)
            } else {
                normalPenWindow.show(it)
            }
        }

        val markPenWindow = markPenWindow()

        binding.mark.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.MARK) {
                viewModel.changeInputType(InputType.MARK)
            } else {
                markPenWindow.show(it)
            }
        }

        val eraserWindow = eraserWindow()

        binding.eraser.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.ERASE) {
                viewModel.changeInputType(InputType.ERASE)
            } else {
                eraserWindow.show(it)
            }
        }

        val laserWindow = laserWindow()

        binding.laser.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.LASER) {
                viewModel.changeInputType(InputType.LASER)
            } else {
                laserWindow.show(it)
            }
        }

        val geometryWindow = geometryWindow()

        binding.geometry.setOnClickListener {
            if (viewModel.currentInputType.value != InputType.GEOMETRY) {
                viewModel.changeInputType(InputType.GEOMETRY)
            } else {
                geometryWindow.show(it)
            }
        }

        val themeWindow = themeWindow()

        binding.theme.setOnClickListener {
            themeWindow.show(it)
        }
    }

    /**
     * 创建普通笔颜色选择面板
     */
    private fun normalPenWindow() = PalettePopup(this).apply {
        addColorSelection(NormalPenStyle.colors, viewModel.normalPenStyle.colorIndex) {
            viewModel.normalPenStyle.colorIndex = it
        }
        addSizeSelection(NormalPenStyle.sizes, viewModel.normalPenStyle.sizeIndex) {
            viewModel.normalPenStyle.sizeIndex = it
        }
    }

    /**
     * 创建马克笔颜色选择面板
     */
    private fun markPenWindow() = PalettePopup(this).apply {
        addColorSelection(MarkPenStyle.colors, viewModel.markPenStyle.colorIndex) {
            viewModel.markPenStyle.colorIndex = it
        }
        addSizeSelection(MarkPenStyle.sizes, viewModel.markPenStyle.sizeIndex) {
            viewModel.markPenStyle.sizeIndex = it
        }
    }

    /**
     * 创建橡皮大小选择面板
     */
    private fun eraserWindow() = PalettePopup(this).apply {
        addSizeSelection(EraserStyle.sizes, viewModel.eraserStyle.sizeIndex) {
            viewModel.eraserStyle.sizeIndex = it
        }
    }

    /**
     * 创建激光笔图形选择面板
     */
    private fun laserWindow() = PalettePopup(this).apply {
        addIconSelection(LaserStyle.icons.keys, viewModel.laserStyle.iconKey) {
            viewModel.laserStyle.iconKey = it
        }
    }

    /**
     * 创建几何图形选择面板
     */
    private fun geometryWindow() = PalettePopup(this).apply {
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

    /**
     * 创建背景主题颜色选择面板
     */
    private fun themeWindow() = PalettePopup(this).apply {
        val colors = intArrayOf(
            BoardThemeType.WHITE.color(),
            BoardThemeType.BLACK.color(),
            BoardThemeType.GREEN.color(),
        )

        addColorSelection(colors, viewModel.theme.value!!.themeType.ordinal) {
            WhiteBoard.setBackgroundColor(colors[it])
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
            WhiteBoard.insertFile(FileConfig(file))
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

            WhiteBoard.insertFile(FileConfig(File(path)))
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

            WhiteBoard.insertFile(FileConfig(File(path)))
        }
    }
}