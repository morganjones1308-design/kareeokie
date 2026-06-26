package com.careeokie

import android.content.Intent
import androidx.car.app.Session
import androidx.car.app.Screen

class CareeokeSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = LyricsCarScreen(carContext)
}
