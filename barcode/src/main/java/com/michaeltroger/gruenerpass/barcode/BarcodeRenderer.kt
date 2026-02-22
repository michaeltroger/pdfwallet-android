package com.michaeltroger.gruenerpass.barcode

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.scale
import com.michaeltroger.gruenerpass.coroutines.dispatcher.di.IoDispatcher
import de.markusfisch.android.zxingcpp.ZxingCpp
import de.markusfisch.android.zxingcpp.ZxingCpp.toBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val BARCODE_SIZE = 400
private const val RENDER_MULTIPLIER = 10
private const val BARCODE_FOREGROUND_COLOR = 0xff000000.toInt()
private const val BARCODE_BACKGROUND_COLOR = 0xffffffff.toInt()

private val preferredPriority = listOf(
    ZxingCpp.BarcodeFormat.AZTEC,
    ZxingCpp.BarcodeFormat.DATA_MATRIX,
    ZxingCpp.BarcodeFormat.PDF_417,
    ZxingCpp.BarcodeFormat.QR_CODE,
    ZxingCpp.BarcodeFormat.UPC_A,
    ZxingCpp.BarcodeFormat.UPC_E,
    ZxingCpp.BarcodeFormat.EAN_8,
    ZxingCpp.BarcodeFormat.EAN_13,
    ZxingCpp.BarcodeFormat.CODE_39,
    ZxingCpp.BarcodeFormat.CODE_93,
    ZxingCpp.BarcodeFormat.CODE_128,
    ZxingCpp.BarcodeFormat.CODABAR,
    ZxingCpp.BarcodeFormat.ITF,
)

private val readerOptions = ZxingCpp.ReaderOptions(
    formats = preferredPriority.toSet(),
    tryHarder = true,
    tryRotate = true,
    tryInvert = true,
    tryDownscale = true,
    maxNumberOfSymbols = 2,
)

private data class BarcodeHit(
    val result: ZxingCpp.Result,
    val cropRect: Rect
)

public interface BarcodeRenderer {
    public suspend fun getBarcodeIfPresent(
        document: Bitmap?,
        tryExtraHard: Boolean,
        generateNewBarcode: Boolean
    ): Bitmap?
}

internal class BarcodeRendererImpl @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : BarcodeRenderer {

    override suspend fun getBarcodeIfPresent(
        document: Bitmap?,
        tryExtraHard: Boolean,
        generateNewBarcode: Boolean,
    ): Bitmap? = withContext(dispatcher) {
        val hit: BarcodeHit = document?.extractBarcode(tryExtraHard) ?: return@withContext null
        if (!isActive) return@withContext null
        encodeBarcodeAsBitmap(
            hit = hit,
            source = document,
            generateNewBarcode = generateNewBarcode
        )
    }

    private suspend fun Bitmap.extractBarcode(
        tryExtraHard: Boolean
    ): BarcodeHit? = withContext(dispatcher) {

        val resultSet = try {
            getCropRectangles(tryExtraHard).flatMap { cropRect ->
                if (!isActive) return@withContext null

                ZxingCpp.readBitmap(
                    bitmap = this@extractBarcode,
                    cropRect = cropRect,
                    rotation = 0,
                    options = readerOptions,
                )?.map { BarcodeHit(it, cropRect) } ?: emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        } catch (_: OutOfMemoryError) {
            emptyList()
        }

        if (resultSet.isEmpty()) return@withContext null

        val resultsMap = resultSet.associateBy { it.result.format }
        preferredPriority.firstNotNullOfOrNull { resultsMap[it] }
    }

    @Suppress("MagicNumber")
    private fun Bitmap.getCropRectangles(tryExtraHard: Boolean): List<Rect> {
        val list = mutableListOf(Rect(0, 0, width, height))
        if (tryExtraHard) {
            list += getCropRectangles(3, 1)
            list += getCropRectangles(4, 1)
            list += getCropRectangles(5, 1)
        }
        return list
    }

    private fun Bitmap.getCropRectangles(
        divisorLongerSize: Int,
        divisorShorterSize: Int
    ): List<Rect> {
        val divisorX: Int
        val divisorY: Int

        if (width > height) {
            divisorX = divisorLongerSize
            divisorY = divisorShorterSize
        } else {
            divisorX = divisorShorterSize
            divisorY = divisorLongerSize
        }

        val tempX = width / divisorX
        val tempY = height / divisorY

        val rects = mutableListOf<Rect>()
        for (x in 0 until divisorX) {
            for (y in 0 until divisorY) {
                rects += Rect(
                    tempX * x,
                    tempY * y,
                    tempX * (x + 1),
                    tempY * (y + 1)
                )
            }
        }
        return rects
    }

    @Suppress("ComplexCondition")
    private fun encodeBarcodeAsBitmap(
        source: Bitmap,
        hit: BarcodeHit,
        generateNewBarcode: Boolean,
    ): Bitmap? {
        return if (generateNewBarcode) {
            val bitMatrix = hit.result.symbol
             if (bitMatrix == null || bitMatrix.data.isEmpty() || bitMatrix.width == 0 || bitMatrix.height == 0) {
                createBarcodeFromBytesOrText(hit.result)
            } else {
                createBarcodeFromBitMatrix(bitMatrix)
            }
        } else {
            cropBarcodeBoundingBox(source, hit)
        }
    }

    private fun createBarcodeFromBitMatrix(bitMatrix: ZxingCpp.BitMatrix): Bitmap {
        val src = bitMatrix.toBitmap(
            setColor = BARCODE_FOREGROUND_COLOR,
            unsetColor = BARCODE_BACKGROUND_COLOR,
        )

        val dstWidth = src.width * RENDER_MULTIPLIER
        val dstHeight = src.height * RENDER_MULTIPLIER

        return src.scale(dstWidth, dstHeight, false)
    }

    private fun createBarcodeFromBytesOrText(originalCode: ZxingCpp.Result): Bitmap {
        val content = if (originalCode.contentType == ZxingCpp.ContentType.TEXT) {
            originalCode.text
        } else {
            originalCode.rawBytes
        }

        return ZxingCpp.encodeAsBitmap(
            content = content,
            format = originalCode.format,
            width = BARCODE_SIZE,
            height = BARCODE_SIZE,
            setColor = BARCODE_FOREGROUND_COLOR,
            unsetColor = BARCODE_BACKGROUND_COLOR,
            margin = 0,
        )
    }

    @Suppress("ReturnCount", "MagicNumber")
    private fun cropBarcodeBoundingBox(
        source: Bitmap,
        hit: BarcodeHit
    ): Bitmap? {
        val pos = hit.result.position
        val cropRect = hit.cropRect

        val xs = intArrayOf(
            pos.topLeft.x + cropRect.left,
            pos.topRight.x + cropRect.left,
            pos.bottomLeft.x + cropRect.left,
            pos.bottomRight.x + cropRect.left
        )

        val ys = intArrayOf(
            pos.topLeft.y + cropRect.top,
            pos.topRight.y + cropRect.top,
            pos.bottomLeft.y + cropRect.top,
            pos.bottomRight.y + cropRect.top
        )

        val minX = xs.min()
        val maxX = xs.max()
        val minY = ys.min()
        val maxY = ys.max()

        if (minX >= maxX || minY >= maxY) return null

        val padX = ((maxX - minX) * 0.1f).toInt()
        val padY = ((maxY - minY) * 0.1f).toInt()

        val left = (minX - padX).coerceAtLeast(0)
        val top = (minY - padY).coerceAtLeast(0)
        val right = (maxX + padX).coerceAtMost(source.width)
        val bottom = (maxY + padY).coerceAtMost(source.height)

        val width = right - left
        val height = bottom - top
        if (width <= 0 || height <= 0) return null

        return Bitmap.createBitmap(source, left, top, width, height)
    }
}
