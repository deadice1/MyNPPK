package com.example.coll.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.coll.R
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class FloorMapContainer @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(ctx, attrs) {

    private lateinit var webView: WebView
    private lateinit var blockLayer: View
    private lateinit var bubbleCard: MaterialCardView
    private lateinit var bubbleScroll: ScrollView
    private lateinit var bubbleText: TextView

    private val jsBridge = MapSvgBridge { nx, ny -> onSvgTap(nx, ny) }

    private var currentAsset: String = "floor2.svg"
    private var loadJob: Job? = null
    private var toggleWired = false
    private var mapGestureExclusionWired = false

    init {
        clipChildren = false
        clipToPadding = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onFinishInflate() {
        super.onFinishInflate()
        webView = findViewById(R.id.floorWebView)
        webView.elevation = 0f
        webView.translationZ = 0f

        webView.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webView.setInitialScale(1)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        webView.addJavascriptInterface(jsBridge, "AndroidMapBridge")
        wireMapGestureExclusion()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.let { evaluateSvgFitAndTap(it) }
            }
        }

        blockLayer = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setBackgroundColor(ContextCompat.getColor(context, R.color.map_scrim))
            visibility = GONE
            isClickable = true
            elevation = dp(8).toFloat()
            translationZ = dp(8).toFloat()
            setOnClickListener { hideCloud() }
        }
        addView(blockLayer, indexOfChild(webView) + 1)

        bubbleText = TextView(context).apply {
            setTextColor(Color.BLACK)
            textSize = 13f
            setLineSpacing(0f, 1.08f)
            setPadding(dp(16), dp(14), dp(16), dp(28))
        }
        bubbleScroll = ScrollView(context).apply {
            isVerticalScrollBarEnabled = true
            isScrollbarFadingEnabled = false
            isFillViewport = false
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            clipToPadding = true
            clipChildren = true
            setPadding(0, 0, 0, dp(12))
            addView(bubbleText, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }

        val bubbleWidth = (resources.displayMetrics.widthPixels * 0.72f).toInt().coerceIn(dp(260), dp(340))

        bubbleCard = MaterialCardView(context).apply {
            radius = dp(40).toFloat()
            strokeWidth = 0
            setCardBackgroundColor(Color.TRANSPARENT)
            background = AppCompatResources.getDrawable(context, R.drawable.map_bubble_cloud_bg)
            visibility = GONE
            clipChildren = false
            cardElevation = dp(8).toFloat()
            elevation = dp(20).toFloat()
            translationZ = dp(20).toFloat()
            // Ставим WRAP_CONTENT, чтобы карточка подстраивалась под текст
            addView(bubbleScroll, LayoutParams(bubbleWidth, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            })
        }


        addView(
            bubbleCard,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = dp(20)
                marginStart = dp(16)
                marginEnd = dp(16)
            },
        )
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun wireMapGestureExclusion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        if (mapGestureExclusionWired) return
        mapGestureExclusionWired = true
        val listener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            applyMapLeftGestureExclusionRects()
        }
        addOnLayoutChangeListener(listener)
        webView.addOnLayoutChangeListener(listener)
        post { applyMapLeftGestureExclusionRects() }
    }

    private fun applyMapLeftGestureExclusionRects() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val w = webView.width
        val h = webView.height
        if (w <= 0 || h <= 0) return
        val dm = resources.displayMetrics
        val maxEdge = minOf(w, (240f * dm.density).toInt())
        val edgePx = minOf(
            maxEdge,
            maxOf((0.38f * w).toInt(), (160f * dm.density).toInt()).coerceAtLeast(1),
        )
        val rects = listOf(Rect(0, 0, edgePx, h))
        webView.systemGestureExclusionRects = rects
        val ch = height
        if (ch > 0) {
            systemGestureExclusionRects = listOf(Rect(0, 0, edgePx.coerceAtMost(width), ch))
        }
    }

    private fun onSvgTap(nx: Float, ny: Float) {
        val room = MapRoomRegistry.findRoomAt(currentAsset, nx, ny) ?: return
        showRoomCloud(room)
    }


    private fun bubbleScrollMaxHeightPx(): Int {
        val dm = resources.displayMetrics
        val fromBottom = dp(88)
        val topReserved = dp(56)
        val h = dm.heightPixels - topReserved - fromBottom
        return h.coerceIn(dp(280), dp(520))
    }

    private fun resolveLifecycleOwner(): LifecycleOwner? {
        findViewTreeLifecycleOwner()?.let { return it }
        return context.findLifecycleOwner()
    }

    private tailrec fun Context.findLifecycleOwner(): LifecycleOwner? = when (this) {
        is LifecycleOwner -> this
        is ContextWrapper -> baseContext.findLifecycleOwner()
        else -> null
    }

    fun setFloorAsset(assetFileName: String) {
        syncFloorAsset(assetFileName)
        webView.loadUrl("file:///android_asset/$assetFileName")
    }


    fun syncFloorAsset(assetFileName: String) {
        currentAsset = assetFileName
        hideCloud()
    }

    private fun showRoomCloud(room: MapRoomDef) {
        loadJob?.cancel()

        val dateStr = MapRoomRegistry.todayApiString()
        val sb = StringBuilder()
        sb.appendLine(room.title)
        sb.appendLine()
        sb.appendLine(room.shortInfo)
        if (!room.responsible.isNullOrBlank()) {
            sb.appendLine()
            sb.append("Заведующий: ").appendLine(room.responsible)
        }

        if (room.id == "CONF_HALL") {
            val events = MapRoomRegistry.conferenceHallEventsBlock(dateStr)
            if (events.isNotBlank()) {
                sb.appendLine()
                sb.appendLine("Мероприятия в конференц-зале")
                sb.appendLine()
                sb.appendLine(events)
            }
        }

        MapRoomRegistry.studentInitiativeLine(room.id)?.let { line ->
            sb.appendLine()
            sb.appendLine("— День студенческих инициатив —")
            sb.appendLine(line)
        }

        bubbleText.text = sb.toString().trimEnd()
        bubbleScroll.scrollTo(0, 0)
        bubbleCard.visibility = VISIBLE
        blockLayer.visibility = VISIBLE
        bringChildToFront(blockLayer)
        bringChildToFront(bubbleCard)

        val scope = resolveLifecycleOwner()?.lifecycleScope
        if (room.fetchScheduleFromApi && room.apiRoomName != null && scope != null) {
            loadJob = scope.launch {
                val res = MapRoomRegistry.loadSchedule(room.apiRoomName, dateStr)
                val scheduleBlock = res.fold(
                    onSuccess = { list -> MapRoomRegistry.scheduleBlockForToday(list) },
                    onFailure = { e ->
                        "\n\nНе удалось загрузить расписание на сегодня: ${e.message ?: "ошибка сети"}"
                    },
                )
                bubbleText.append(scheduleBlock)
                bubbleText.requestLayout()
                bubbleScroll.post {
                    bubbleScroll.scrollTo(0, 0)
                }
            }
        }
    }

    private fun hideCloud() {
        loadJob?.cancel()
        loadJob = null
        bubbleCard.visibility = GONE
        blockLayer.visibility = GONE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (toggleWired) return
        val toggle = rootView.findViewById<MaterialButtonToggleGroup>(R.id.floorToggleGroup) ?: return
        toggleWired = true
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val asset = when (checkedId) {
                R.id.btnFloorBasement -> "floor_basement.svg"
                R.id.btnFloor1 -> "floor1.svg"
                R.id.btnFloor2 -> "floor2.svg"
                R.id.btnFloor3 -> "floor3.svg"
                else -> return@addOnButtonCheckedListener
            }
            setFloorAsset(asset)
        }
    }

    companion object {

        fun evaluateSvgFitAndTap(webView: WebView) {
            webView.evaluateJavascript(SVG_FIT_AND_TAP_JS, null)
        }

        private const val SVG_FIT_AND_TAP_JS = """
            (function(){
              function rootSvg(){
                var el=document.documentElement;
                if(el&&el.localName==='svg')return el;
                return document.querySelector('svg');
              }
              function fit(){
                var svg=rootSvg();
                if(!svg)return;
                
                var vb = svg.getAttribute('viewBox');
                if (!vb) {
                    var w = svg.getAttribute('width') || svg.style.width;
                    var h = svg.getAttribute('height') || svg.style.height;
                    if (w && h) {
                        w = parseFloat(w);
                        h = parseFloat(h);
                        svg.setAttribute('viewBox', '0 0 ' + w + ' ' + h);
                    }
                }

                svg.setAttribute('width','100%');
                svg.setAttribute('height','100%');
                svg.style.width = '100%';
                svg.style.height = '100%';
                svg.setAttribute('preserveAspectRatio','xMidYMid meet');
                
                if (document.body) {
                    document.body.style.margin = '0';
                    document.body.style.padding = '0';
                    document.body.style.overflow = 'hidden';
                }
                if (document.documentElement) {
                    document.documentElement.style.margin = '0';
                    document.documentElement.style.padding = '0';
                    document.documentElement.style.overflow = 'hidden';
                }
              }
              function clientXY(ev){
                if(ev.changedTouches&&ev.changedTouches.length){
                  var t=ev.changedTouches[0];
                  return {x:t.clientX,y:t.clientY};
                }
                return {x:ev.clientX,y:ev.clientY};
              }
              function bind(){
                var svg=rootSvg();
                if(!svg||svg.dataset.npMapTap)return;
                svg.dataset.npMapTap='1';
                function send(ev){
                  if(ev.pointerType==='mouse'&&ev.button!==0)return;
                  var xy=clientXY(ev);
                  var pt=svg.createSVGPoint();
                  pt.x=xy.x;pt.y=xy.y;
                  var ctm=svg.getScreenCTM();
                  if(!ctm)return;
                  var inv=ctm.inverse();
                  if(!inv)return;
                  var p=pt.matrixTransform(inv);
                  var vb=svg.viewBox.baseVal;
                  var vw=vb.width||1,vh=vb.height||1;
                  var minX=vb.x,minY=vb.y,maxX=vb.x+vw,maxY=vb.y+vh;
                  if(p.x<minX||p.x>maxX||p.y<minY||p.y>maxY)return;
                  var nx=(p.x-vb.x)/vw,ny=(p.y-vb.y)/vh;
                  if(window.AndroidMapBridge)AndroidMapBridge.onSvgTap(nx,ny);
                }
                svg.addEventListener('pointerup',function(ev){ send(ev); },{capture:true,passive:true});
              }
              fit();
              bind();
            })();
        """
    }
}
