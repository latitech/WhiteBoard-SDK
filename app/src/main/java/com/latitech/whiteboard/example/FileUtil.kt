// Created by 超悟空 on 2017/10/20.

package com.latitech.whiteboard.example

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

/**
 * 解析FileProvider的工具
 **/
object FileUtil {

    fun getPathFromUri(context: Context, uri: Uri): String? = getPathFromRemoteUri(context, uri)

    private fun getPathFromRemoteUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val suffix = getFileExtension(getFileName(context, uri))

                val file = File.createTempFile("document", suffix, context.externalCacheDir ?: context.cacheDir)

                file.outputStream().use {
                    inputStream.copyTo(it)
                }

                file.path
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

    private fun getFileExtension(fileName: String?): String? {
        if (fileName == null) {
            return null
        }

        val dotIndex = fileName.lastIndexOf(".")
        if (dotIndex > 0 && fileName.length > dotIndex) {
            return fileName.substring(dotIndex)
        }
        return null
    }

    /**
     * 创建临时图片路径
     */
    fun createImagePath(context: Context): String {
        val dir = context.externalCacheDir ?: context.cacheDir
        return dir.path + File.separator + System.currentTimeMillis() + ".jpg"
    }

    /**
     * 创建临时图片Uri
     *
     * @return 路径uri
     * filePath 图片路径
     */
    fun createImageUri(context: Context, filePath: String): Uri = if (Build.VERSION.SDK_INT < 24) {
        File(filePath).toUri()
    } else {
        FileProvider.getUriForFile(context, context.packageName + ".provider", File(filePath))
    }
}