package com.latitech.whiteboard.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.latitech.whiteboard.WhiteBoard
import com.latitech.whiteboard.example.databinding.ActivityRoomBinding
import com.latitech.whiteboard.listener.ScreenshotsCallback
import com.latitech.whiteboard.model.FileConfig
import com.latitech.whiteboard.model.InputConfig
import org.jetbrains.anko.alert
import java.io.File
import java.nio.ByteBuffer

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
     * 临时图片路径
     */
    private var imageTempPath = ""

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

        WhiteBoard.joinRoom(intent.getParcelableExtra(ROOM_DATA_TAG)!!)
    }

    override fun onContentChanged() {

        binding.whiteBoard.setZOrderMediaOverlay(true)

        binding.insertImage.setOnClickListener {
            openPicture.launch("image/*")
        }

        binding.camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                imageTempPath = FileUtil.createImagePath(this)
                openCamera.launch(FileUtil.createImageUri(this, imageTempPath))
            } else {
                requestPermissionCamera.launch(Manifest.permission.CAMERA)
            }
        }

        binding.insertFile.setOnClickListener {
            openFile.launch("*/*")
        }

        binding.select.setOnClickListener {
            WhiteBoard.setInputMode(InputConfig.select())
        }

        binding.pan.setOnClickListener {
            WhiteBoard.setInputMode(InputConfig.pen(Color.BLACK, 2f))
        }

        var black = true

        binding.panColor.setOnClickListener {
            black = !black
            if (black) {
                // 普通笔
                WhiteBoard.setInputMode(InputConfig.pen(Color.BLACK, 2f))
            } else {
                // 荧光笔
                WhiteBoard.setInputMode(InputConfig.pen(0x5F8F5CF3, 30f))
            }
        }

        binding.eraser.setOnClickListener {
            WhiteBoard.setInputMode(InputConfig.erase(20f))
        }

        binding.restore.setOnClickListener {
            WhiteBoard.revert()
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
    }

    private val requestPermissionCamera =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    imageTempPath = FileUtil.createImagePath(this)
                    openCamera.launch(FileUtil.createImageUri(this, imageTempPath))
                }
            }

    private val openCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        val file = File(imageTempPath)

        if (file.exists()) {
            WhiteBoard.insertFile(FileConfig(file))
        }
    }

    private val openPicture = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            return@registerForActivityResult
        }

        val path = FileUtil.getPathFromUri(this, it)

        if (path != null) {
            Log.v(TAG, "onPictureSuccess path:$path")

            WhiteBoard.insertFile(FileConfig(File(path)))
        }
    }

    private val openFile = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            return@registerForActivityResult
        }

        val path = FileUtil.getPathFromUri(this, it)

        if (path != null) {
            Log.v(TAG, "onFileSuccess path:$path")

            WhiteBoard.insertFile(FileConfig(File(path)))
        }
    }
}