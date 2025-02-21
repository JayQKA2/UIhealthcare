package vn.edu.usth.uihealthcare.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.edu.usth.uihealthcare.R

class HeartRateAdapter(private var heartDataList: List<HeartData>) : RecyclerView.Adapter<HeartRateAdapter.HeartRateViewHolder>() {

    fun updateData(newHeartDataList: List<HeartData>) {
        heartDataList = newHeartDataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeartRateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_heart_data, parent, false)
        return HeartRateViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeartRateViewHolder, position: Int) {
        val heartData = heartDataList[position]
        holder.bind(heartData)
    }

    override fun getItemCount(): Int = heartDataList.size

    class HeartRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val heartRateTextView: TextView = itemView.findViewById(R.id.heartnumber)

        fun bind(heartData: HeartData) {
            dateTextView.text = heartData.date
            timeTextView.text = heartData.time
            heartRateTextView.text = heartData.heartRate
        }
    }
}