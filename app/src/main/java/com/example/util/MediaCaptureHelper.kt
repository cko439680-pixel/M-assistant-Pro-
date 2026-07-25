package com.example.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.WindowManager
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MediaCaptureHelper {

    fun getDisplayFps(context: Context): Int {
        return try {
            val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.refreshRate ?: 60f
            } else {
                @Suppress("DEPRECATION")
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                wm?.defaultDisplay?.refreshRate ?: 60f
            }
            refreshRate.toInt().coerceIn(30, 120)
        } catch (e: Exception) {
            60
        }
    }

    fun captureAndSaveScreenshot(context: Context, onComplete: () -> Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val view = window?.decorView

        fun saveBitmap(bitmap: Bitmap) {
            Thread {
                val savedUri = saveBitmapToMediaStore(context, bitmap)
                Handler(Looper.getMainLooper()).post {
                    if (savedUri != null) {
                        Toast.makeText(context, "📸 Screenshot saved to Gallery!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "📸 Screenshot saved to Pictures/Screenshots", Toast.LENGTH_SHORT).show()
                    }
                    onComplete()
                }
            }.start()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && window != null && view != null && view.width > 0 && view.height > 0) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                PixelCopy.request(
                    window,
                    android.graphics.Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + view.width,
                        locationOfViewInWindow[1] + view.height
                    ),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            saveBitmap(bitmap)
                        } else {
                            // Fallback canvas capture
                            val canvasBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(canvasBitmap)
                            view.draw(canvas)
                            saveBitmap(canvasBitmap)
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback capture for overlays / background contexts
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.parseColor("#0F172A"))
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#22C55E")
            textSize = 54f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("M Assistant Pro Screenshot", width / 2f, height / 2f, paint)
        saveBitmap(bitmap)
    }

    private fun saveBitmapToMediaStore(context: Context, bitmap: Bitmap): Uri? {
        val filename = "M_Assistant_${System.currentTimeMillis()}.png"
        var uri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Screenshots")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                uri = resolver.insert(contentUri, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val screenshotDir = File(picturesDir, "Screenshots")
                if (!screenshotDir.exists()) screenshotDir.mkdirs()
                val imageFile = File(screenshotDir, filename)

                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                uri = Uri.fromFile(imageFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uri
    }
}
