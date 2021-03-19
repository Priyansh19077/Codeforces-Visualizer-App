package com.example.codeforcesviewer.UserData.Styling

import android.content.Context
import androidx.core.content.ContextCompat.getColor
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis

class BarChartStyling(private val chart: BarChart, private val context: Context) {

    fun styleIt(color: Int) {

        //position of x-axis
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        //labels on axis
        chart.axisRight.setDrawLabels(false)

        //extra text
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        //animation
        chart.animateY(2000)

        //borders
        chart.setDrawBorders(true)
        chart.setBorderWidth(1.5f)

        //transparent background
        chart.setDrawGridBackground(false)

        //axis text color
        chart.xAxis.textColor = getColor(context, color)
        chart.axisLeft.textColor = getColor(context, color)

        //elevation of chart // shadow effect
        chart.elevation = 100f

        //setting scale
        chart.setScaleEnabled(false)

        //clicking, highlighting and zoom
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.setPinchZoom(false)

        //grid lines on each axis
        chart.axisRight.setDrawGridLines(false)

        //labels on each axis
        chart.xAxis.labelCount = 5
        chart.axisLeft.labelCount = 4

        //offset
        chart.setExtraOffsets(0f, 0f, 0f, 0f)
    }
}