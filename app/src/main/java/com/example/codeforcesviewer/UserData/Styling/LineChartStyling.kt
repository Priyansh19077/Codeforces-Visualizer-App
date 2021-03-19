package com.example.codeforcesviewer.UserData.Styling

import android.content.Context
import androidx.core.content.ContextCompat.getColor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis

class LineChartStyling(private val chart: LineChart, private val context: Context) {
    fun styleIt(color: Int) {

        //axis labels
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.setDrawLabels(false)

        //animation
        chart.animateX(2000)

        //axis grid lines
        chart.axisRight.setDrawGridLines(false)

        //borders
        chart.setDrawBorders(true)
        chart.setBorderWidth(1.5f)

        //other text
        chart.legend.isEnabled = false
        chart.description.isEnabled = false

        //x-axis position
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        //axis label color
        chart.axisLeft.textColor = getColor(context, color)

        //elevation
        chart.elevation = 10f

        //scale
        chart.setScaleEnabled(false)

        //clicking, highlighting and zoom
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.setPinchZoom(false)
    }
}