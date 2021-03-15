package com.example.codeforcesviewer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.codeforcesviewer.UserData.Contests

class contestExpandableListAdapter(val context : Context, val headings : List<String>, val contests : List<Contests>) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return headings.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return contests.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return headings[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return contests[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var convertView1 = convertView
        if(convertView1 == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView1 = inflater.inflate(R.layout.contest_heading, null)
        }
        return convertView1
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var convertView1 = convertView
        if(convertView1 == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView1 = inflater.inflate(R.layout.contest_item, null)
        }
        if(convertView1 != null) {
            convertView1.findViewById<TextView>(R.id.contest_number).text = childPosition.toString()
            convertView1.findViewById<TextView>(R.id.contest_name).text = contests[childPosition].contestName
            convertView1.findViewById<TextView>(R.id.contest_rank).text = contests[childPosition].rank.toString()
            convertView1.findViewById<TextView>(R.id.contest_rating_change).text = (contests[childPosition].newRating - contests[childPosition].oldRating).toString()
            convertView1.findViewById<TextView>(R.id.contest_new_rating).text = contests[childPosition].newRating.toString()
        }
        return convertView1
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false;
    }
}
