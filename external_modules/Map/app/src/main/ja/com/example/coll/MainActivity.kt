package com.example.coll

import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup

class MainActivity : AppCompatActivity() {

    private lateinit var floorTitleText: TextView
    private lateinit var floorToggleGroup: MaterialButtonToggleGroup
    private lateinit var floorWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        floorTitleText = findViewById(R.id.floorTitleText)
        floorToggleGroup = findViewById(R.id.floorToggleGroup)
        floorWebView = findViewById(R.id.floorWebView)

        floorToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btnFloorBasement -> loadFloor("Подвал", "floor_basement.svg")
                R.id.btnFloor1 -> loadFloor("1 этаж", "floor1.svg")
                R.id.btnFloor2 -> loadFloor("2 этаж", "floor2.svg")
                R.id.btnFloor3 -> loadFloor("3 этаж", "floor3.svg")
            }
        }

        // Этаж по умолчанию
        floorToggleGroup.check(R.id.btnFloor2)
    }

    private fun loadFloor(title: String, assetFile: String) {
        floorTitleText.text = title
    }

    override fun onDestroy() {
        floorWebView.destroy()
        super.onDestroy()
    }
}
