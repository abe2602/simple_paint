package com.example.paint.drawing_view

import android.graphics.Path
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

class CustomPath(var color: Int, var brushThickness: Float) : Path()