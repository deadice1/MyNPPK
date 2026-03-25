package com.example.coll.data

import android.graphics.Path
import android.graphics.RectF

/**
 * Область кабинета на карте SVG
 */
data class RoomArea(
    val roomId: String,
    val path: Path,
    val bounds: RectF
)

