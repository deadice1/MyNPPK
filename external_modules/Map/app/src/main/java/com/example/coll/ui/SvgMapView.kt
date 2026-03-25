package com.example.coll.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.example.coll.data.RoomArea
import java.io.InputStream

/**
 * Кастомный View для отображения SVG карты с интерактивными областями
 */
class SvgMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var svg: SVG? = null
    private var originalSvgWidth: Float = 0f
    private var originalSvgHeight: Float = 0f
    private var roomAreas: List<RoomArea> = emptyList()
    private var selectedRoomId: String? = null
    private var debugMode: Boolean = false
    private var lastClickX: Float = 0f
    private var lastClickY: Float = 0f
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f
    
    private val highlightPaint = Paint().apply {
        style = Paint.Style.FILL
        color = 0x40FF0000.toInt() // Полупрозрачный красный
        isAntiAlias = true
    }
    
    private val debugPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = 0xFFFF0000.toInt() // Красный
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = 0xFFFF0000.toInt()
        textSize = 24f
        isAntiAlias = true
    }
    
    var onRoomClickListener: ((String) -> Unit)? = null
    var onDebugClickListener: ((Float, Float) -> Unit)? = null

    /**
     * Загружает SVG из ресурсов
     */
    fun loadSvgFromAssets(assetPath: String) {
        try {
            Log.d("SvgMapView", "Начинаю загрузку SVG: $assetPath")
            val inputStream: InputStream = context.assets.open(assetPath)
            Log.d("SvgMapView", "Файл открыт, размер: ${inputStream.available()} байт")
            
            svg = SVG.getFromInputStream(inputStream)
            Log.d("SvgMapView", "SVG объект создан: ${svg != null}")
            
            svg?.let {
                // Получаем размеры из SVG, если они не заданы, используем viewBox
                val docWidth = it.documentWidth
                val docHeight = it.documentHeight
                val viewBox = it.documentViewBox
                
                Log.d("SvgMapView", "documentWidth=$docWidth, documentHeight=$docHeight")
                Log.d("SvgMapView", "viewBox width=${viewBox.width()}, height=${viewBox.height()}")
                
                // Используем documentWidth/Height если они заданы, иначе viewBox
                originalSvgWidth = if (docWidth > 0) docWidth else viewBox.width()
                originalSvgHeight = if (docHeight > 0) docHeight else viewBox.height()
                
                // Если размеры все еще 0, используем значения по умолчанию из viewBox
                if (originalSvgWidth <= 0 || originalSvgHeight <= 0) {
                    originalSvgWidth = viewBox.width()
                    originalSvgHeight = viewBox.height()
                }
                
                // Если и это не помогло, используем значения по умолчанию из SVG файла (1800x1000)
                if (originalSvgWidth <= 0 || originalSvgHeight <= 0) {
                    originalSvgWidth = 1800f
                    originalSvgHeight = 1000f
                    Log.w("SvgMapView", "⚠️ Используются размеры по умолчанию: 1800x1000")
                }
                
                Log.d("SvgMapView", "✅ SVG загружен успешно!")
                Log.d("SvgMapView", "   Размеры: width=$originalSvgWidth, height=$originalSvgHeight")
                
                // Обновляем View после загрузки SVG
                post {
                    requestLayout()
                    invalidate()
                    Log.d("SvgMapView", "Вызваны requestLayout() и invalidate() в post")
                }
            } ?: Log.e("SvgMapView", "❌ SVG объект равен null!")
        } catch (e: Exception) {
            Log.e("SvgMapView", "❌ Ошибка загрузки SVG: ${e.message}", e)
            Log.e("SvgMapView", "Тип ошибки: ${e.javaClass.simpleName}")
            e.printStackTrace()
        }
    }

    /**
     * Загружает SVG из ресурсов drawable
     */
    fun loadSvgFromResources(resourceId: Int) {
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            svg = SVG.getFromInputStream(inputStream)
            svg?.let {
                originalSvgWidth = it.documentWidth
                originalSvgHeight = it.documentHeight
            }
            invalidate()
        } catch (e: SVGParseException) {
            e.printStackTrace()
        }
    }

    /**
     * Устанавливает области кабинетов для интерактивности
     */
    fun setRoomAreas(areas: List<RoomArea>) {
        roomAreas = areas
        invalidate()
    }

    /**
     * Выделяет выбранный кабинет
     */
    fun setSelectedRoom(roomId: String?) {
        selectedRoomId = roomId
        invalidate()
    }
    
    /**
     * Включает/выключает режим отладки
     * В режиме отладки при клике показываются координаты
     */
    fun setDebugMode(enabled: Boolean) {
        debugMode = enabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Проверяем, что View измерен
        if (width <= 0 || height <= 0) {
            Log.w("SvgMapView", "⚠️ Размеры view равны 0: width=$width, height=$height - пропускаем отрисовку")
            return
        }
        
        // Проверяем, что SVG загружен
        if (svg == null) {
            Log.w("SvgMapView", "⚠️ SVG не загружен - пропускаем отрисовку")
            return
        }
        
        // Проверяем, что размеры SVG установлены
        if (originalSvgWidth <= 0 || originalSvgHeight <= 0) {
            Log.w("SvgMapView", "⚠️ Размеры SVG не установлены - пропускаем отрисовку")
            return
        }
        
        svg?.let { svgDocument ->
            if (originalSvgWidth > 0 && originalSvgHeight > 0) {
                // Вычисляем масштаб для отображения SVG
                val widthScale = width / originalSvgWidth
                val heightScale = height / originalSvgHeight
                
                // Сохраняем масштаб для пропорционального отображения
                val scale = minOf(widthScale, heightScale)
                
                // Устанавливаем размеры view для SVG с сохранением пропорций
                val scaledWidth = originalSvgWidth * scale
                val scaledHeight = originalSvgHeight * scale
                
                svgDocument.documentWidth = scaledWidth
                svgDocument.documentHeight = scaledHeight
                
                // Рисуем SVG
                try {
                    svgDocument.renderToCanvas(canvas)
                    Log.d("SvgMapView", "✅ SVG отрисован успешно!")
                    Log.d("SvgMapView", "   View размер: $width x $height")
                    Log.d("SvgMapView", "   SVG размер: ${originalSvgWidth} x ${originalSvgHeight}")
                    Log.d("SvgMapView", "   Масштаб: $scale")
                    Log.d("SvgMapView", "   Document размер: $scaledWidth x $scaledHeight")
                } catch (e: Exception) {
                    Log.e("SvgMapView", "❌ Ошибка отрисовки SVG: ${e.message}", e)
                    Log.e("SvgMapView", "   Stack trace: ${e.stackTraceToString()}")
                }
                
                // Сохраняем реальный масштаб для преобразования координат
                scaleX = scale
                scaleY = scale
                
                // Выделяем выбранный кабинет (преобразуем координаты)
                selectedRoomId?.let { roomId ->
                    roomAreas.find { it.roomId == roomId }?.let { area ->
                        val scaledPath = Path(area.path)
                        val matrix = Matrix()
                        matrix.setScale(scaleX, scaleY)
                        scaledPath.transform(matrix)
                        canvas.drawPath(scaledPath, highlightPaint)
                    }
                }
                
                // Режим отладки: показываем координаты последнего клика
                if (debugMode && lastClickX > 0 && lastClickY > 0) {
                    // Рисуем крестик в точке клика
                    canvas.drawLine(lastClickX - 20, lastClickY, lastClickX + 20, lastClickY, debugPaint)
                    canvas.drawLine(lastClickX, lastClickY - 20, lastClickX, lastClickY + 20, debugPaint)
                    
                    // Преобразуем координаты клика обратно в координаты SVG
                    val svgX = lastClickX / scaleX
                    val svgY = lastClickY / scaleY
                    
                    // Показываем координаты (и в координатах View, и в координатах SVG)
                    val text = "View: X=${lastClickX.toInt()}, Y=${lastClickY.toInt()}\nSVG: X=${svgX.toInt()}, Y=${svgY.toInt()}"
                    canvas.drawText(text, lastClickX + 30, lastClickY - 30, textPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            
            // В режиме отладки сохраняем координаты
            if (debugMode) {
                lastClickX = x
                lastClickY = y
                invalidate()
                // Преобразуем координаты клика в координаты SVG
                val svgX = if (scaleX > 0) x / scaleX else x
                val svgY = if (scaleY > 0) y / scaleY else y
                onDebugClickListener?.invoke(svgX, svgY)
                return true
            }
            
            // Преобразуем координаты клика в координаты SVG для проверки
            val svgX = if (scaleX > 0) x / scaleX else x
            val svgY = if (scaleY > 0) y / scaleY else y
            
            // Проверяем, попал ли клик в область какого-либо кабинета
            roomAreas.forEach { area ->
                // Сначала проверяем границы для быстрой проверки
                if (area.bounds.contains(svgX, svgY)) {
                    // Затем проверяем точное попадание в Path с помощью Region
                    val boundsRect = android.graphics.Rect(
                        area.bounds.left.toInt(),
                        area.bounds.top.toInt(),
                        area.bounds.right.toInt(),
                        area.bounds.bottom.toInt()
                    )
                    val region = Region().apply {
                        setPath(area.path, Region(boundsRect))
                    }
                    if (region.contains(svgX.toInt(), svgY.toInt())) {
                        selectedRoomId = area.roomId
                        invalidate()
                        onRoomClickListener?.invoke(area.roomId)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        var measuredWidth = widthSize
        var measuredHeight = heightSize
        
        if (originalSvgWidth > 0 && originalSvgHeight > 0) {
            // Сохраняем пропорции SVG
            val aspectRatio = originalSvgHeight / originalSvgWidth
            
            if (widthMode == MeasureSpec.EXACTLY) {
                measuredWidth = widthSize
                measuredHeight = (widthSize * aspectRatio).toInt()
                
                // Проверяем, не превышает ли высота доступное пространство
                if (heightMode == MeasureSpec.AT_MOST && measuredHeight > heightSize) {
                    measuredHeight = heightSize
                    measuredWidth = (heightSize / aspectRatio).toInt()
                }
            } else if (heightMode == MeasureSpec.EXACTLY) {
                measuredHeight = heightSize
                measuredWidth = (heightSize / aspectRatio).toInt()
            } else {
                // Оба режима AT_MOST или UNSPECIFIED
                measuredWidth = widthSize
                measuredHeight = (widthSize * aspectRatio).toInt()
                
                if (measuredHeight > heightSize) {
                    measuredHeight = heightSize
                    measuredWidth = (heightSize / aspectRatio).toInt()
                }
            }
        } else {
            // Если SVG еще не загружен, используем минимальные размеры
            if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
                measuredHeight = heightSize.coerceAtLeast(400)
            }
        }
        
        setMeasuredDimension(measuredWidth, measuredHeight)
        Log.d("SvgMapView", "onMeasure: $measuredWidth x $measuredHeight (SVG: $originalSvgWidth x $originalSvgHeight)")
    }
}

