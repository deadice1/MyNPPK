package com.example.coll.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coll.R
import com.example.coll.data.ScheduleItem

/**
 * Adapter для отображения расписания
 */
class ScheduleAdapter(private val scheduleItems: List<ScheduleItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(scheduleItems[position])
    }

    override fun getItemCount() = scheduleItems.size

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val subjectTextView: TextView = itemView.findViewById(R.id.subjectTextView)
        private val teacherTextView: TextView = itemView.findViewById(R.id.teacherTextView)
        private val groupTextView: TextView = itemView.findViewById(R.id.groupTextView)

        fun bind(item: ScheduleItem) {
            timeTextView.text = item.time
            subjectTextView.text = item.subject
            teacherTextView.text = item.teacher
            groupTextView.text = item.group
        }
    }
}

