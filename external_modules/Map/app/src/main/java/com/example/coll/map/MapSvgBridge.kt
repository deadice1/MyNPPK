package com.example.coll.map

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class MapSvgBridge(private val onTap: (Float, Float) -> Unit) {

    @JavascriptInterface
    fun onSvgTap(nx: Double, ny: Double) {
        Handler(Looper.getMainLooper()).post {
            onTap(nx.toFloat().coerceIn(0f, 1f), ny.toFloat().coerceIn(0f, 1f))
        }
    }
}
