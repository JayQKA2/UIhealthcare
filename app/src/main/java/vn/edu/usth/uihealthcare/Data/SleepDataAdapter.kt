package vn.edu.usth.uihealthcare.Data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.edu.usth.uihealthcare.R

class SleepDataAdapter(private var sleepDataList: List<SleepData>) : RecyclerView.Adapter<SleepDataAdapter.SleepDataViewHolder>() {

    class SleepDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeRangeTextView: TextView = itemView.findViewById(R.id.timeRangeTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepDataViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep_data, parent, false)
        return SleepDataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SleepDataViewHolder, position: Int) {
        val currentItem = sleepDataList[position]
        holder.dateTextView.text = currentItem.date
        holder.timeRangeTextView.text = currentItem.timeRange
        holder.durationTextView.text = currentItem.duration
    }

    override fun getItemCount() = sleepDataList.size

    fun updateData(newData: List<SleepData>) {
        sleepDataList = newData
        notifyDataSetChanged()
    }
}