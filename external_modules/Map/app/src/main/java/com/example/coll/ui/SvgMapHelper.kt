package com.example.coll.ui

import android.graphics.Path
import android.graphics.RectF
import com.example.coll.data.RoomArea

/**
 * Вспомогательный класс для создания областей кабинетов из координат
 */
object SvgMapHelper {
    
    /**
     * Создает прямоугольную область кабинета
     * @param roomId ID кабинета
     * @param x1 левая координата X
     * @param y1 верхняя координата Y
     * @param x2 правая координата X
     * @param y2 нижняя координата Y
     */
    fun createRectArea(roomId: String, x1: Float, y1: Float, x2: Float, y2: Float): RoomArea {
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y1)
            lineTo(x2, y2)
            lineTo(x1, y2)
            close()
        }
        val bounds = RectF(x1, y1, x2, y2)
        return RoomArea(roomId, path, bounds)
    }
    
    /**
     * Создает многоугольную область кабинета из массива точек
     * @param roomId ID кабинета
     * @param points массив координат [x1, y1, x2, y2, ...]
     */
    fun createPolygonArea(roomId: String, points: FloatArray): RoomArea {
        require(points.size >= 6 && points.size % 2 == 0) {
            "Points array must contain at least 3 points (6 values) and be even"
        }
        
        val path = Path().apply {
            moveTo(points[0], points[1])
            for (i in 2 until points.size step 2) {
                lineTo(points[i], points[i + 1])
            }
            close()
        }
        
        val bounds = RectF().apply {
            path.computeBounds(this, true)
        }
        
        return RoomArea(roomId, path, bounds)
    }
    
    /**
     * Создает круглую область кабинета
     * @param roomId ID кабинета
     * @param centerX координата X центра
     * @param centerY координата Y центра
     * @param radius радиус
     */
    fun createCircleArea(roomId: String, centerX: Float, centerY: Float, radius: Float): RoomArea {
        val path = Path().apply {
            addCircle(centerX, centerY, radius, Path.Direction.CW)
        }
        val bounds = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        return RoomArea(roomId, path, bounds)
    }
}

