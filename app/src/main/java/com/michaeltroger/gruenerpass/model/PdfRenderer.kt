package com.michaeltroger.gruenerpass.model

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.io.File

private const val QR_CODE_SIZE = 400
private const val MAX_BITMAP_SIZE = 100 * 1024 * 1024


class PdfRenderer(private val context: Context) {

    private val file = File(context.filesDir, PDF_FILENAME)
    private val documentWidth = 500

    private val activityManager: ActivityManager?
        get() = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    private val qrCodeReader = QRCodeReader()
    private val qrCodeWriter = MultiFormatWriter()

    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    private val counterContext = newSingleThreadContext("CounterContext")

    suspend fun loadFile(): Boolean = withContext(counterContext) {
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(fileDescriptor!!)
            return@withContext true
        } catch (exception: Exception) {
            return@withContext false
        }
    }

    fun getPageCount(): Int {
        return renderer!!.pageCount
    }

    fun onCleared() {
        renderer?.use {}
        fileDescriptor?.use {}
    }

    suspend fun getQrCodeIfPresent(pageIndex: Int): Bitmap? = withContext(counterContext) {
       return@withContext renderPage(pageIndex).extractQrCodeIfAvailable()
    }

    suspend fun renderPage(pageIndex: Int): Bitmap = withContext(counterContext) {
        if (renderer == null) {
            loadFile()
        }
        return@withContext renderer!!.openPage(pageIndex).renderAndClose()
    }

    private fun PdfRenderer.Page.renderAndClose(): Bitmap = use {
        val bitmap = createBitmap()
        render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        bitmap
    }

    private fun PdfRenderer.Page.createBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            documentWidth, (documentWidth.toFloat() / documentWidth * height).toInt(), Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        return bitmap
    }

    private fun Bitmap.extractQrCodeIfAvailable(): Bitmap? {
        try {
            val intArray = IntArray(width * height)
            getPixels(intArray, 0, width, 0, 0, width, height)
            val source: LuminanceSource = RGBLuminanceSource(width, height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            qrCodeReader.decode(binaryBitmap).text?.let {
                return encodeQrCodeAsBitmap(it)
            }
        } catch (ignore: Exception) {}
        return null
    }

    private fun encodeQrCodeAsBitmap(source: String): Bitmap? {
        val result: BitMatrix = try {
            val hintMap = HashMap<EncodeHintType, Any>()
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.Q
            qrCodeWriter.encode(source, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hintMap)
        } catch (ignore: Exception) {
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)

        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmapQrCode = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        bitmapQrCode!!.setPixels(pixels, 0, QR_CODE_SIZE, 0, 0, w, h)
        return bitmapQrCode
    }
}