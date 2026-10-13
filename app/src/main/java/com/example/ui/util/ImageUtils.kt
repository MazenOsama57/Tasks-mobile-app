package com.example.ui.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Resource-efficient image decoder that applies explicit downsampling and
 * memory-constrained bitmap configuration to prevent HWUI decoding buffer overflow warnings.
 * Also prioritizes vector SVG drawables for crisp infinite-resolution rendering without bitmap memory.
 */
object ImageUtils {

    /**
     * Calculates the optimal inSampleSize factor for memory-constrained decoding.
     */
    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize.coerceAtLeast(1)
    }

    /**
     * Decodes a bitmap from resources with explicit size constraints and downsampling.
     */
    fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int,
        reqWidth: Int = 300,
        reqHeight: Int = 300
    ): Bitmap? {
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions without allocating memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeResource(res, resId, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            // Memory constrained configuration: RGB_565 halves memory compared to ARGB_8888
            options.inPreferredConfig = Bitmap.Config.RGB_565

            BitmapFactory.decodeResource(res, resId, options)
        } catch (e: OutOfMemoryError) {
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodes a bitmap from an input stream with explicit downsampling.
     */
    fun decodeSampledBitmapFromStream(
        inputStreamProvider: () -> InputStream,
        reqWidth: Int = 300,
        reqHeight: Int = 300
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStreamProvider().use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565

            inputStreamProvider().use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Resource-efficient Compose image composable that:
 * 1. Natively renders Vector/SVG XML drawables with zero pixelation and zero bitmap decoding overhead.
 * 2. Uses explicit downsampling and memory-constrained decoding (RGB_565) for raster resources,
 *    preventing Android HWUI "Image decoding logging dropped" warnings.
 */
@Composable
fun ResourceEfficientImage(
    resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    reqWidth: Int = 300,
    reqHeight: Int = 300
) {
    val context = LocalContext.current
    val isXmlVector = remember(resId) {
        try {
            val typeName = context.resources.getResourceTypeName(resId)
            typeName == "drawable"
        } catch (e: Exception) {
            true
        }
    }

    if (isXmlVector) {
        // Compose painterResource processes Android vector XML (SVG equivalent) directly
        // via VectorPainter without raster bitmap allocation.
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = resId) {
            value = withContext(Dispatchers.IO) {
                ImageUtils.decodeSampledBitmapFromResource(
                    context.resources,
                    resId,
                    reqWidth = reqWidth,
                    reqHeight = reqHeight
                )
            }
        }
        val bitmap = bitmapState.value
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}
