package com.example.codeforcesviewer.UserData

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforcesviewer.R

class ContestAdapter(
        private val context : Context,
        private val dataset : List<ContestDataToShow>) : RecyclerView.Adapter<ContestAdapter.ItemViewHolder>(){
    class ItemViewHolder(private val view : View) : RecyclerView.ViewHolder(view){
        val contestNumber: TextView = view.findViewById(R.id.contest_number)
        val contestName: TextView = view.findViewById(R.id.contest_name)
        val contestRank: TextView = view.findViewById(R.id.contest_rank)
        var contestRatingChange: TextView = view.findViewById(R.id.contest_rating_change)
        val contestNewRating : TextView = view.findViewById(R.id.contest_new_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.contest_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.contestNewRating.text = item.newRating
        holder.setIsRecyclable(false)
        holder.contestRank.text = item.rank
        holder.contestNumber.text = item.number
        holder.contestName.text = item.name
        holder.contestRatingChange.text = item.ratingChange
        if(item.ratingChangeColor != -1)
            holder.contestRatingChange.setTextColor(getColor(context, item.ratingChangeColor))
        if(item.newRatingColor != -1)
            holder.contestNewRating.setTextColor(getColor(context, item.newRatingColor))
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}