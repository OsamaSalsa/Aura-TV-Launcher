package com.example.ui.common

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap

fun drawableToBitmapSafe(drawable: Drawable): Bitmap {
    return try {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap
        } else {
            drawable.toBitmap(width = 128, height = 128)
        }
    } catch (_: Exception) {
        createBitmap(128, 128)
    }
}
