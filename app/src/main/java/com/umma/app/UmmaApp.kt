package com.umma.app

import android.app.Application
import com.umma.app.data.local.AppDatabase

class UmmaApp : Application() {
    val database by lazy { AppDatabase.get(this) }
}