package com.example.coll.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coll.R
import com.example.coll.data.Room
import com.example.coll.data.RoomRepository
import com.example.coll.data.ScheduleItem
import com.google.android.material.appbar.MaterialToolbar

/**
 * Activity для отображения расписания кабинета
 */
class ScheduleActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: ""
        val room = getRoomById(roomId)
        
        toolbar.title = room?.name ?: "Расписание"
        
        val recyclerView = findViewById<RecyclerView>(R.id.scheduleRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ScheduleAdapter(room?.schedule ?: emptyList())
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    private fun getRoomById(roomId: String): Room? {
        // Здесь можно загрузить данные из базы данных или API
        // Пока используем тестовые данные
        return RoomRepository.getRoomById(roomId)
    }
    
    companion object {
        const val EXTRA_ROOM_ID = "room_id"
    }
}

