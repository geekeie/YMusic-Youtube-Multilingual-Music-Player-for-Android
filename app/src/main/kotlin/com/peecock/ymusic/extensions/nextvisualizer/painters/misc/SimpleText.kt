package com.peecock.ymusic.extensions.nextvisualizer.painters.misc

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.peecock.ymusic.extensions.nextvisualizer.painters.Painter
import com.peecock.ymusic.extensions.nextvisualizer.utils.VisualizerHelper

class SimpleText(
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE },
    var text: String = "",
    //
    var x: Float = 100f,
    var y: Float = 100f
    ) : Painter() {

    override fun calc(helper: VisualizerHelper) {
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        canvas.drawText(text, x, y, paint)
    }
}